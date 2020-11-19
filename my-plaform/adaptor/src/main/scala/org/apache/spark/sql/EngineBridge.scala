package org.apache.spark.sql

import java.io.CharArrayWriter

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.catalyst.json.JacksonGenerator


class EngineBridge {


}

object EngineBridge{
  def toJson(dataFrame: DataFrame): String= {
    val rowSchema = dataFrame.schema
    val writer = new CharArrayWriter()
    val gen = new JacksonGenerator(rowSchema, writer)
    val encoder = RowEncoder.apply(rowSchema).resolveAndBind()
    val res = dataFrame.collect().map(row=>{
      gen.write(encoder.toRow(row))
      gen.flush()
      val json = writer.toString
      json
    })
    gen.close()
    res.mkString(" ")
  }
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("testApp").master("local").getOrCreate()

    val df = sparkSession.createDataFrame(Seq(
      ("ming", 20, 15552211521L),
      ("hong", 19, 13287994007L),
      ("zhi", 21, 15552211523L)
    )) toDF("name", "age", "phone")

    df.show()

//    val data: Dataset[String] = df.toJSON
    val jsonStr = toJson(df)
//    println(data.collect())
    println("jsonStr:"+jsonStr)
  }
}
