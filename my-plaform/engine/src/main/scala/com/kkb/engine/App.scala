package com.kkb.engine

import akka.actor.ActorSystem
import com.kkb.common.AkkaUtils
import com.kkb.engine.interpreter.SparkInterpreter
import com.kkb.utils.{GlobalConfigUtils, ZKUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.spark.SparkConf

/**
 * 平台服务启动的入口
 */
object App {

  /**
   * app里面包含main，也就意味着，我们可以把打成jar，每次运行的时候传入的参数不一样，
   * 所有我们要对传入的参数做一些解析成自己想要的结构
   *
   * @param args
   * @return
   */
  def parseArgs(args: Array[String]): Map[String, String] = {
    var argsMap: Map[String, String] = Map()
    //("-engine.zkServers",2,3)
    //("-engine.tag",3,5)
    var argv: List[String] = args.toList
    while (argv.nonEmpty) {
      argv match {
        /**
         * fun(a,b,c){
         *
         * }
         * fun(1,2,3)
         */
        //()->(tail)-(value,tail)->("-engine.zkServers",value,tail)
        //()->("-engine.zkServers",value,tail)
        // a ++;

        case "-engine.zkServers" :: value :: tail => {
          argsMap += ("zkServers" -> value)
          argv = tail
        }
        case "-engine.tag" :: value :: tail => {
          argsMap += ("engine.tag" -> value)
          argv = tail
        }
        case Nil =>
        //()->(tail)
        case tail => {
          println(s"对不起，无法识别：${tail.mkString(" ")}")
        }

      }
    }

    argsMap
  }

  def main(args: Array[String]): Unit = {
    val argv = parseArgs(args)
    System.setProperty("HADOOP_USER_NAME","root")
    //构建spark的解析器
    val interpreter = new SparkInterpreter
    //start的核心功能创建sparkILoop对象(解析代码)
    val sparkConf: SparkConf = interpreter.start()
    sparkConf.set("spark.driver.host", "localhost")
    //获取zk集群的ip和端口信息
    val zkServer = argv.getOrElse("zkServers", GlobalConfigUtils.getProp("zk.servers"))
    val client: ZkClient = ZKUtils.getZkClient(zkServer)

    println(zkServer)
    println(client)

    val actorConf = AkkaUtils.getConfig(client)
    val actorSystem = ActorSystem("system", actorConf)

    val hostname = actorConf.getString("akka.remote.netty.tcp.hostname")
    val port = actorConf.getString("akka.remote.netty.tcp.port")

    val engineSession = new EngineSession(s"${hostname}:${port}", argv.get("engine.tag"))
    println("engineSession:" + engineSession)

    //任务并行度,默认值是6
    val parlism: Int = sparkConf.getInt(config.PARALLELISM.key, config.PARALLELISM.defaultValue.get)


    (1 to parlism).foreach(id => {
      actorSystem.actorOf(JobActor.apply(interpreter, engineSession, sparkConf), name = s"actor_${id}")
    })
  }
}
