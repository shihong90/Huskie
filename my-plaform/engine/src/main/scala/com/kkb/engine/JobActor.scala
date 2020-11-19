package com.kkb.engine

import java.io.{ByteArrayOutputStream, PrintStream}

import akka.actor.{Actor, Props}
import com.cartravel.engine.antlr.{EngineBaseListener, EngineLexer, EngineParser}
import com.kkb.domain.{CommandMode, JobStatus, ResultDataType}
import com.kkb.domain.engine.{Instruction, Job}
import com.kkb.engine.interpreter.Interpreter.ExecuteResponse
import com.kkb.engine.interpreter.SparkInterpreter
import com.kkb.logging.Logging
import com.kkb.utils.{GlobalConfigUtils, ZKUtils}
import org.I0Itec.zkclient.ZkClient
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{EngineBridge, SparkSession}

class JobActor(
                _interpreter: SparkInterpreter,
                engineSession: EngineSession,
                sparkConf: SparkConf
              ) extends Actor with Logging {


  //组装jobActor的有效路径，存储到zookeeper（前端通过这个路径，可以获取akka引擎，然后来做前后端对接的操作）
  val valid_engine_path = s"${ZKUtils.valid_engine_path}/${engineSession.engineInfo}_${context.self.path.name}"
  var zkClient: ZkClient = _
  var token: String = _
  var job: Job = _

  var sparkSession: SparkSession = _
  var interpreter: SparkInterpreter = _

  override def preStart(): Unit = {
    warn(
      """
        |               __                           __                 __
        |_____    _____/  |_  ___________    _______/  |______ ________/  |_
        |\__  \ _/ ___\   __\/  _ \_  __ \  /  ___/\   __\__  \\_  __ \   __\
        | / __ \\  \___|  | (  <_> )  | \/  \___ \  |  |  / __ \|  | \/|  |
        |(____  /\___  >__|  \____/|__|    /____  > |__| (____  /__|   |__|
        |     \/     \/                         \/            \/
      """.stripMargin)
    zkClient = ZKUtils.getZkClient(GlobalConfigUtils.getProp("zk.servers"))
    interpreter = _interpreter
    sparkSession = EngineSession.createSpark(sparkConf).newSession()

    ZKUtils.registerActorInPlatEngine(zkClient, valid_engine_path, engineSession.tag.getOrElse("default"))
  }

  /**
    * 没收到一条消息此方法调用一次
    *
    * @return
    */
  override def receive: Receive = {
    case Instruction(commandMode, instruction, variables, _token) => {
      actorHook() { () => {
        var assemble_instruction = instruction
        token = _token
        job = Job(instruction = instruction, variables = variables)
        //获取groupId
        val groupId = GlobalGroupId.getGroupId
        job.engineInfoAndGroup = s"${engineSession.engineInfo}_${groupId}"
        //将job的信息响应到客户端
        sender() ! job

        sparkSession.sparkContext.clearJobGroup()
        sparkSession.sparkContext.setJobDescription(s"runing job:${instruction}")
        sparkSession.sparkContext.setJobGroup("groupId:" + groupId, "instruction:" + instruction)

        commandMode match {
          case CommandMode.SQL => {
            val engineSQLExecListener = new EngineSQLExecListener(sparkSession)
            //            JobActor.parseRule(assemble_instruction,engineSQLExecListener)
            parseSQL(assemble_instruction, engineSQLExecListener)
          }

          case CommandMode.CODE => {
            job.mode = CommandMode.CODE

            /**
              * 处理前：
              * line1；
              * line2;
              * line3;
              *
              * 处理后:
              * line1；line2;line3;
              */
            assemble_instruction = assemble_instruction.replaceAll("'", "\"").replaceAll("\n", " ")
            val response = interpreter.execute(assemble_instruction)
            while (!storageJobStatus(response)) {
              warn("任务还没有结束!")
            }
          }

        }
      }
      }
    }
  }

  override def postStop(): Unit = {
    warn(
      """
        |             _                       _
        |  ____  ____| |_  ___   ____     ___| |_  ___  ____
        | / _  |/ ___)  _)/ _ \ / ___)   /___)  _)/ _ \|  _ \
        |( ( | ( (___| |_| |_| | |      |___ | |_| |_| | | | |
        | \_||_|\____)\___)___/|_|      (___/ \___)___/| ||_/
        |                                              |_|
      """.stripMargin)
    interpreter.close()
    sparkSession.stop()
  }

  def storageJobStatus(response: ExecuteResponse): Boolean = {
    true
  }

  /**
    * 钩子：通过钩子能够找到操作的对象,类似引用
    * 作用:捕获业务处理异常，方便发送到客户端进行跟踪
    *
    * @param func
    */
  def actorHook()(func: () => Unit): Unit = {
    try {
      func()
    } catch {
      case e: Exception => {
        val out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out))
        val job = Job(data = out.toString(), dataType = ResultDataType.ERROR)
        //通过sender 把job对象发送到客户端
        sender ! job
      }
    }
  }

  /**
    * 上报任务执行结果
    *
    * @param listener
    */
  def reportRestult(listener: EngineSQLExecListener): Unit = {
    val hdfs_path = s"hdfs://node01:9000/tmp/engine/result/sql_result_${System.currentTimeMillis()}"
    job.hdfs_path = hdfs_path
    //封装任务状态信息
    listener.result() match {
      case x if (x.containsKey("tmpTable")) => {
        val tableDataFrame = sparkSession.table(listener.getResult("tmpTable"))

        tableDataFrame.show(20)

        job.data = EngineBridge.toJson(tableDataFrame).mkString("[", ",", "]")
        job.schema = tableDataFrame.schema.fields.map(_.name).mkString(",")
        job.jobStatus = JobStatus.FINISH
        job.takeTime = System.currentTimeMillis() - job.startTime.getTime
        engineSession.batchJob.put(job.engineInfoAndGroup, job)
        tableDataFrame.write.json(hdfs_path)
      }
      case x if (x.containsKey("explainStr")) => {
        job.data = listener.getResult("explainStr")
        job.dataType = ResultDataType.PRE
        job.jobStatus = JobStatus.FINISH
        job.takeTime = System.currentTimeMillis() - job.startTime.getTime
        engineSession.batchJob.put(job.engineInfoAndGroup, job)
      }
      case _ => {}
    }
  }


  /**
    * 把规则解析和结果汇报进行一次封装
    */
  def parseSQL(input: String, listener: EngineSQLExecListener): Unit = {
    try {
      JobActor.parseRule(input, listener)
      reportRestult(listener)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        job.success = false;
        val out = new ByteArrayOutputStream()
        e.printStackTrace(new PrintStream(out))
        //存储异常信息
        job.data = new String(out.toByteArray)
        job.dataType = ResultDataType.ERROR
        out.close();
      }
      case _ =>
    }
    job.jobStatus = JobStatus.FINISH
    engineSession.batchJob.put(job.engineInfoAndGroup, job)
  }

}

object JobActor {
  def parseRule(input: String, listener: EngineBaseListener): Unit = {
    val loadLexer = new EngineLexer(new ANTLRInputStream(input))
    val tokens = new CommonTokenStream(loadLexer)
    var parser = new EngineParser(tokens)
    val state = parser.statement()
    ParseTreeWalker.DEFAULT.walk(listener, state)
  }

  def apply(sparkInterpreter: SparkInterpreter, engineSession: EngineSession, sparkConf: SparkConf): Props = {
    Props(new JobActor(sparkInterpreter, engineSession, sparkConf))
  }
}
