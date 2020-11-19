import java.net.InetAddress

import akka.actor.ActorSystem
import akka.pattern.Patterns
import akka.util.Timeout
import com.kkb.domain.CommandMode
import com.kkb.domain.engine.Instruction
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration

object Client extends App {
  //客户端Actor ip
  val host = InetAddress.getLocalHost().getHostAddress
  //客户端Actor 端口
  val port = 3001

  val conf = ConfigFactory.parseString(
    s"""
       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname = ${host}
       |akk.remote.netty.tcp.port = ${port}
       |""".stripMargin
  )

  //词频统计命令
  //  val instruction =
  //    "val textFile = spark.sparkContext.textFile(\"hdfs://node01:9000/words\");"+
  //    "val counts = textFile.flatMap(line=>line.split(\" \")).map(word=>(word,1)).reduceByKey(_+_);"+
  //    "counts.repartition(1).saveAsTextFile(\"hdfs://node01:9000/words_count\")"

  //  val instruction ="load text. `hdfs://node01:9000/test.dd` as tb;\n select * from tb;"
  //  val instruction ="load text. `hdfs://node01:9000/test.dd` as tb;\n"+
  //                    "save tb as text.`hdfs://node01:9000/test_data/textData` "+
  //                    "where coalesce 5"

  //    val instruction ="load text. `hdfs://node01:9000/test.dd` as tb;\n "+
  //                     "explain select * from tb;"


  //  val instruction = "load jdbc.db \n" +
  //    "where driver=\"com.mysql.jdbc.Driver\" \n" +
  //    "and url=jdbc:mysql://node02:3306/mysql?characterEncoding=utf8 \n      " +
  //    "and user=root \n      " +
  //    "and password=!Qaz123456 \n" +
  //    "as tb; \n" +
  //    "SELECT * FROM tb LIMIT 100"

  //  val instruction = "load jdbc.db \n" +
  //    "where driver=\"com.mysql.jdbc.Driver\" \n      " +
  //    "and url=\"jdbc:mysql://node02:3306/mysql?characterEncoding=utf8\" \n      " +
  //    "and user=\"root\" \n      " +
  //    "and password=\"!Qaz123456\" \n" +
  //    "as tb; \n" +
  //    "SELECT * FROM tb LIMIT 100"

  //  val instruction = "load csv.`file:///d:\\test_data\\test.csv` as tb; "+
  //  "save update tb as jdbc.`test7` \n "+
  //  "where driver=\"com.mysql.jdbc.Driver\" \n "+
  //  "and url=\"jdbc:mysql://node02:3306/test?characterEncoding=utf8\" \n "+
  //  "and user=\"root\" \n "+
  //  "and password=\"!Qaz123456\";\""


  //  val instruction = "load kafka.users " +
  //    "where maxRatePerPartition=\"10\"" +
  //    "and `group.id`=\"kkb\"" +
  //    "and autoCommitOffset=\"false\"" +
  //    "and `data.type`= \"json\"" +
  //    "and `kafka.bootstrap.servers`=\"node01:9092\"" +
  //    "and `zk.servers`=\"node01:2181\" as tb ;"
  //  "\n select * from tb;\""

  //  val instruction = "load redis.runoobkey " +
  //    "where " +
  //    "host=\"node02\"" +
  //    "and port=\"6379\"" +
  //    "and dbNum=\"10\"" +
  //    "and schema =\"true\"" +
  //    "as tb;" +
  //    "select * from tb;"

  //  val instruction = " load orc.`hdfs://node01:9000/test_data/part.orc` as tb;"+
  //  "save tb as redis.test \n"+
  //  "where partition=\"11\""+
  //  "and host=\"node02\""+
  //  "and port=\"6379\""+
  //  "and password=\"\""+
  //  "and expire=\"20\""+
  //  "and db=\"9\""+
  //  "and column=\"integer_column\";"

    val instruction = " load orc.`hdfs://node01:9000/test_data/part.orc` as tb1111;"+
//  "select string_column from tb1111 where  integer_column in (select integer_column from tb1111); "
  "select date_column,(select string_column from tb1111) as column from  tb1111 ; "
//  val instruction = "load es.`user/users` " +
//    "and `es.nodes`=\"node01,node02,node03\"" +
//    "as tEs;"+
//    " load orc.`hdfs://node01:9000/test_data/part.orc` as tOrc;"+
//    "select * from tEs;"+
//    "select * from tOrc;"+
//  "select tEs.alliance_name,tOrc.integer_column from tEs join tOrc on tEs.create_user = tOrc.integer_column ;"

  //  val instruction = "load jdbc.db \n" +
  //    "where driver=\"com.mysql.jdbc.Driver\" \n      " +
  //    "and url=\"jdbc:mysql://node02:3306/mysql?characterEncoding=utf8\" \n      " +
  //    "and user=\"root\" \n      " +
  //    "and password=\"!Qaz123456\" \n" +
  //    "as tb; \n" +
  //    "SELECT * FROM tb LIMIT 100; \n" +
  //    "save append tb as es.`cc/bb` where `es.nodes`=\"node01,node02,node03\";"


  val comandMode = CommandMode.SQL
  //系统预留的参数变量
  val variables = "[]"
  val token = ""


  //创建客户端的actorsystem系统
  val clientSystem = ActorSystem("client", conf)

  /**
    * 服务端配置信息
    */
  val ip = InetAddress.getLocalHost().getHostAddress
  val actorAddr = s"${ip}:3000"
  val actorName = "actor_1"
  val selection = clientSystem.actorSelection("akka.tcp://system@" + actorAddr + "/user/" + actorName)
  Patterns.ask(selection, Instruction(comandMode, instruction, variables, token), new Timeout(Duration.create(10, "s")))

}
