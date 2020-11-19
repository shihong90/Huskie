package com.kkb.engine.interpreter

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import org.apache.spark.SparkConf
import org.apache.spark.repl.SparkILoop

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.JPrintWriter
import scala.tools.nsc.interpreter.Results.Result

/**
 * Created by like
 */
class SparkInterpreter extends AbstractSparkInterpreter{
  private var sparkConf: SparkConf = _
  private var sparkILoop: SparkILoop = _

  override protected def isStarted(): Boolean = {
    sparkILoop != null
  }

  /**
   * 只有代码调用次方法
   * @param order
   * @return
   */
  override protected def interpret(order: String): Result = {
    //spark local方式运行的时候才会创建sparkILoop对象
        sparkILoop.interpret(order)
//    val tmpOrder = "val textFile = spark.sparkContext.textFile(\"hdfs://node01:9000/words\");val counts = textFile.flatMap(line => line.split(\" \")).map(word => (word, 1)).reduceByKey(_ + _);counts.repartition(1).saveAsTextFile(\"hdfs://node01:9000/words_count\");"
//    sparkILoop.interpret(order)
  }

  override protected def bind(name: String, className: String, value: Object, modifier: List[String]): Unit = {
    sparkILoop.beQuietDuring{
      sparkILoop.bind(name , className , value , modifier)
    }
  }

  override def close(): Unit = {
    super.close()
    if(sparkILoop != null){
      sparkILoop.closeInterpreter()
      sparkILoop = null
    }
  }

  /** 注册spark的repl交互，并返回sparkConf */
  override def start(): SparkConf = {
    require(sparkILoop == null)
    val conf = new SparkConf()
    //如果指定了spark.repl.classdir配置，会用这个配置的路径作为class文件的输出路径， java.io.tmpdir系统临时目录
    val rootDir = conf.get("spark.repl.classdir" , System.getProperty("java.io.tmpdir"))
    val outputDir = Files.createTempDirectory(Paths.get(rootDir) , "spark").toFile
    outputDir.deleteOnExit()
    //注册使用spark的repl（Spark Shell的实现是建立在Scala REPL的基础上的）
    conf.set("spark.repl.class.outputDir" , outputDir.getAbsolutePath)
    //指定spark的二进制包
    val execUri = System.getenv("SPARK_EXECUTOR_URL")
    if(execUri != null){
      conf.set("spark.executor.uri" , execUri)
    }
    if(System.getenv("SPARK_HOME") != null){
      conf.setSparkHome(System.getenv("SPARK_HOME"))
    }

    val cl = ClassLoader.getSystemClassLoader
    //加载JDK环境
    /**
     * "val textFile = sc.textFile()"
     * 动态字节码生成->依赖asm(jdk下)->加载JDK环境->通过asm动态生成字节码对象(JVM内存中)
     */
    val jars = (getUserJars(conf) ++ cl.asInstanceOf[java.net.URLClassLoader].getURLs.map(_.toString)).mkString(File.pathSeparator)
    val settings = new Settings()
    /**
     * Spark2.1.0的REPL基于Scala-2.11的scala.tools.nsc编译工具实现，代码已经相当简洁，
     * Spark给interp设置了2个关键的配置: -Yrepl-class-based和-Yrepl-outdir，
     * 通过这两个配置，我们在shell中输入的代码会被编译为class文件输出到执行的文件夹中。
     * 如果指定了spark.repl.classdir配置，会用这个配置的路径作为class文件的输出路径
     * */
    settings.processArguments(List(
      "-Yrepl-class-based",
      "-Yrepl-outdir",
      s"${outputDir.getAbsolutePath}",
      "-classpath",
      jars
    ), true
    )
    settings.usejavacp.value = true
    settings.embeddedDefaults(Thread.currentThread().getContextClassLoader())

    sparkILoop = new SparkILoop(None , new JPrintWriter(outputStream , true))
    sparkILoop.settings = settings
    sparkILoop.createInterpreter()
    sparkILoop.initializeSynchronous()

    restoreContextClassLoader{
      sparkILoop.setContextClassLoader()
      sparkConf = createSparkConf(conf)
    }

    sparkConf
  }

  /**根据sparkConf的参数，获取用户的jar包*/
  def getUserJars(conf:  SparkConf , isShell:Boolean = false):Seq[String] = {
    val sparkJars = conf.getOption("spark.jars")
    //如果是spark on yarn 则合并spark的jar和yarn的jar路径
    if(isShell && conf.get("spark.master") == "yarn"){
      val yarnJars = conf.getOption("spark.yarn.dist.jars")
      //将spark的jar和yarn的jar路径合并
      unionFileLists(sparkJars , yarnJars).toSeq
    }else{
      sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
    }
  }



  /**jar包的路径合并
   * jdk目录下的jar和spark目录下的jar的路径做合并*/
  def unionFileLists(leftList:Option[String] , rightList:Option[String]):Seq[String] = {
    var allFiles = Seq[String]()
    leftList.foreach(value => allFiles ++= value.split(","))
    rightList.foreach(value =>  allFiles ++= value.split(","))
    allFiles.filter(_.nonEmpty)
  }
}
