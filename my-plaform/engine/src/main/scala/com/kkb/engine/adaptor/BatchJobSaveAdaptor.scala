package com.kkb.engine.adaptor

import com.kkb.engine.EngineSQLExecListener
import com.kkb.utils.GlobalConfigUtils
import org.apache.spark.sql.{DataFrame, DataFrameWriter, Row, SaveMode}

class BatchJobSaveAdaptor(
                           engineSQLExecListener: EngineSQLExecListener,
                           data: DataFrame,
                           savePath: String,
                           tableName: String,
                           format: String,
                           mode: SaveMode,
                           partitionByCol: Array[String],
                           numPartition: Int,
                           option: Map[String, String]
                         ) {
  //定义的是一个函数
  val parse = {
    var writer: DataFrameWriter[Row] = data.write
    writer = writer.format(format).mode(mode).partitionBy(partitionByCol: _*).options(option)
    format match {
      case "json" | "orc" | "parquet" | "csv" => {

      }
      case "text" => {
        data.rdd.repartition(numPartition).saveAsTextFile(savePath)
      }
      case "hbase" => {}
      case "jdbc" => {
        writer
          .format("org.apache.spark.sql.execution.customDatasource.jdbc")
          .option("driver", option.getOrElse("driver", GlobalConfigUtils.getProp("jdbc.driver")))
          .option("url", option.getOrElse("url", GlobalConfigUtils.getProp("jdbc.url")))
          .option("dbtable", savePath)
          .save()
      }
      case "hive" => {}
      case "mongo" => {}
      case "redis" => {
        writer
          .option("host", option.getOrElse("host", GlobalConfigUtils.getProp("redis.host")))
          .option("port", option.getOrElse("port", GlobalConfigUtils.getProp("redis.port")))
          .option("password", option.getOrElse("password", ""))
          .option("ttl", option.getOrElse("expire", ""))
          .option("db", option.getOrElse("db", "0"))
          .option("key.column", option.getOrElse("column", "_spark"))
          .option("table", savePath)
          .format("org.apache.spark.sql.execution.customDatasource.redis")
          .save()
      }
      case "es" => {
        val options = Map(
          "pushdown" -> "true",
          "es.nodes" -> s"${option.getOrElse("es.nodes", "localhost")}",
          "es.port" -> "9200"
        )
        writer.format("org.elasticsearch.spark.sql").mode(mode).save(savePath)
      }
      case _ =>
    }
  }


}
