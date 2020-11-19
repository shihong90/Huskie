package com.kkb.engine.interpreter

import java.io.ByteArrayOutputStream

import com.kkb.engine.EngineSession
import org.apache.spark.SparkConf
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.JsonDSL._

import scala.tools.nsc.interpreter.Results


object AbstractSparkInterpreter {
  //反向肯定预测，匹配命令当中的换行符
  //命令:就是要执行的代码中的换行符
  private[interpreter] val KEEP_NEWLINE_REGEX = """(?<=\n)""".r
}

abstract  class AbstractSparkInterpreter extends Interpreter {

  import AbstractSparkInterpreter._

  //操作流，返回到客户端的响应
  protected val outputStream = new ByteArrayOutputStream()

  //方便底层实现，用于判断是否已经启动引擎(类似命令中执行spark-shell)
  protected def isStarted(): Boolean

  /**是否中断spark-shell*/
  protected def interpret(order:String):Results.Result

  //对spark-shell绑定初始化的变量值
  protected def bind(
            name: String,
            className: String,
            value: Object,
            modifier: List[String]
          )

  //关闭spark-shell
  override def close(): Unit = {}

  /**读取流中的数据*/
  private def readStdout() = {
    val output = outputStream.toString("UTF-8")
    outputStream.reset()
    output
  }

  /**解析错误信息*/
  protected[interpreter] def parseError(stdout:String):(String , Seq[String]) = {
    //按照换行符解析
    val lines = KEEP_NEWLINE_REGEX.split(stdout)
    //获取堆栈信息
    val traceback = lines.tail
    //获取错误详情
    val errValues = lines.headOption.map(_.trim).getOrElse("UNKNOW ERROR")
    (errValues , traceback)
  }

  /**解析出执行的命令*/
  private def executeLine(order:String): Interpreter.ExecuteResponse = {
    scala.Console.withOut(outputStream){
      interpret(order) match{
        case Results.Success => Interpreter.ExecuteSuccess(TEXT_PLAIN -> readStdout())

        case Results.Error =>
          val tuple: (String, Seq[String]) = parseError(readStdout())
          Interpreter.ExecuteError("ERROR" , tuple._1 , tuple._2)

        case Results.Incomplete => Interpreter.ExecuteIncomplete()
      }
    }
  }

  /**
   * 打破双亲委托机制的，优先加载子类
   * 双亲委派模型的作用:保证JDK核心类的优先加载
   * 缺陷：如果想执行自己的spark-shell， 不想执行spark的，原则违背双亲委派机制
   * 解决：打破双亲委派机制
   * 方式：
   * 1、自定义类加载器，重写loadClass方法；
   * 2、使用线程上下文类加载器；
   *
   * spark启动的时候已经加载spark的classpath路径下的jar包，使用线程上下文切换的方式解析执行
   * 交互的命令
   * */
  protected def restoreContextClassLoader[T](fn: => T):T = {
    val contextClassLoader = Thread.currentThread().getContextClassLoader
    try{
      fn
    }finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader)
    }
  }

  /**解析scala-shell的代码*/
  private def executeLines(lines:List[String] , resultFromLastLine:Interpreter.ExecuteResponse):Interpreter.ExecuteResponse = {
    lines match{
      case Nil => resultFromLastLine //如果为空，则默认返回成功

      case head :: tail =>
        val result = executeLine(head)
        result match{
          case Interpreter.ExecuteSuccess(data) =>
            val response = resultFromLastLine match {
              /**结果成功*/
              case Interpreter.ExecuteSuccess(ds) =>
                //合并输入的代码内容TEXT_PLAIN -> 内容合并
                //JObject((TEXT_PLAIN, JString(""))
                if (data.values.contains(TEXT_PLAIN) && ds.values.contains(TEXT_PLAIN)) {
                  val lastRet = data.values.getOrElse(TEXT_PLAIN, "").asInstanceOf[String]
                  val currentRet = ds.values.getOrElse(TEXT_PLAIN, "").asInstanceOf[String]

                  if (lastRet.nonEmpty && currentRet.nonEmpty) {
                    Interpreter.ExecuteSuccess(TEXT_PLAIN -> s"${lastRet}${currentRet}")
                  } else if (lastRet.nonEmpty) {
                    Interpreter.ExecuteSuccess(TEXT_PLAIN -> lastRet)
                  } else if (currentRet.nonEmpty) {
                    Interpreter.ExecuteSuccess(TEXT_PLAIN -> currentRet)
                  } else {
                    result
                  }
                } else {
                  result
                }

              /**结果失败*/
              case Interpreter.ExecuteError(_ , _ , _) => result

              /**结果终止*/
              case Interpreter.ExecuteAborted(_) => result

              /**结果未完成*/
              case Interpreter.ExecuteIncomplete() =>
                tail match{
                  case Nil =>
                    executeLine(s"{\n$head\n}") match{
                      //ExecuteIncomplete could be caused by an actual incomplete statements or statements with just comments.
                      //If it is an actual incomplete statement, the interpreter will return an error.
                      case Interpreter.ExecuteIncomplete() | Interpreter.ExecuteError(_,_,_) => result
                      //If it is some comment, the interpreter will return success.
                      case _ => resultFromLastLine
                    }
                  case next :: nextTail =>
                    //To distinguish them, reissue the same statement wrapped in { }.
                    //If it is some comment, the interpreter will return success.
                    executeLines(head + "\n" + next :: nextTail, resultFromLastLine)
                }

              case _ => result
            }
            executeLines(tail, response)
        }

    }
  }

  /**解析代码*/
  override def execute(order: String):Interpreter.ExecuteResponse = {
    //打破双亲委派模型，程序会有限走executeLines这个方法
    restoreContextClassLoader{
      require(isStarted())
      executeLines(order.trim.split("\n").toList , Interpreter.ExecuteSuccess(
        JObject(
          (TEXT_PLAIN , JString(""))
        )
      ))
    }
  }


  def createSparkConf(conf:SparkConf): SparkConf = {
    val spark = EngineSession.createSpark(conf)
    //用于变量的绑定
    bind("spark", spark.getClass.getCanonicalName, spark, List("""@transient"""))
    execute("import org.apache.spark.SparkContext._ \n import spark.implicits._")
    //    execute("import spark.implicits._")
    execute("import spark.sql")
    execute("import org.apache.spark.sql.functions._")
    spark.sparkContext.getConf
  }
}
