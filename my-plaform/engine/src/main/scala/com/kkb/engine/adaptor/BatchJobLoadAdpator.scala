package com.kkb.engine.adaptor

import java.util.UUID

import com.kkb.engine.EngineSQLExecListener
import com.kkb.engine.`trait`.ParseLogicalTools
import com.kkb.utils.GlobalConfigUtils
import org.apache.spark.sql.execution.SQLExecution
import org.apache.spark.sql.execution.SQLExecution.EXECUTION_ID_KEY
import org.apache.spark.sql.{DataFrame, DataFrameReader}

class BatchJobLoadAdpator(
                         engineSQLExecListener: EngineSQLExecListener,
                         var format:String,
                         var path:String,
                         var tableName:String,
                         option:Map[String,String]
                         ) extends ParseLogicalTools{
  val sparkSession = engineSQLExecListener.sparkSession

  val parse={
    var table:DataFrame = null
    val frameReader :DataFrameReader = engineSQLExecListener.sparkSession.read
    frameReader.options(option)

    format match{
      case "jdbc"=>
        frameReader
          .option("dbtable",path)
          .option("driver",option.getOrElse("driver",GlobalConfigUtils.getProp("jdbc.driver")))
          .option("url",option.getOrElse("url",GlobalConfigUtils.getProp("jdbc.url")))
        table = frameReader.format("org.apache.spark.sql.execution.customDatasource.jdbc").load()
      case "hbase"=>
      case "es" =>
        val options = Map(
          "pushdown"->"true",
          "es.nodes"->s"${option.getOrElse("es.nodes","localhost")}",
          "es.port"->"9200"
        )
        table = frameReader.format("org.elasticsearch.spark.sql").options(options).load(path)
      case "kafka"=>
        frameReader
          .option("bootstrap.servers",option.getOrElse("kafka.bootstrap.servers",GlobalConfigUtils.getProp("kafka.bootstrap.servers")))
          .option("zk.servers",option.getOrElse("zk.servers",GlobalConfigUtils.getProp("zk.servers")))
          .option("topics",path)
        table = frameReader.format("org.apache.spark.sql.execution.customDatasource.kafka").load()
        if(option.getOrElse("data.type","json").toLowerCase().equals("json")){
          //_.getString(0)获取到row里面的第一列
          table = sparkSession.read.json(table.select("msg").rdd.map(_.getString(0)))
        }
      case "json"|"csv"|"orc"|"parquet"|"text"=>
        table  = frameReader.option("header","true").format(format).load(path)
      case "xml"=>
      case "redis"=>
        frameReader
          .option("host", option.getOrElse("host", "localhost"))
          .option("port", option.getOrElse("port", "6379"))
          .option("auth", option.getOrElse("auth", null))
          .option("dbNum", option.getOrElse("dbNum", "0"))
          .option("timeout", option.getOrElse("timeout", "2000"))
          .option("infer.schema", option.getOrElse("schema", "true"))
          .option("keys.pattern", path)
        table = frameReader.format("org.apache.spark.sql.execution.customDatasource.redis").load()

      case "jsonStr"=>
      case _=>
    }

    table.createOrReplaceTempView(tableName)
    //
    engineSQLExecListener.sparkSession.sparkContext.setLocalProperty(EXECUTION_ID_KEY,null)
    val retDf = engineSQLExecListener.sparkSession.sql(s"select * from ${tableName}")
    retDf.show(10)

  }


}
