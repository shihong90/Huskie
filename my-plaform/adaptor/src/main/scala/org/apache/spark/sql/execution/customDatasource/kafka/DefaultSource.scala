package org.apache.spark.sql.execution.customDatasource.kafka

import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, RelationProvider, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.kafka.{KafkaUtils, OffsetRange}

class DefaultSource extends RelationProvider {
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    val fieldName: String = parameters.getOrElse("fieldName", "msg")
    new KafkaRelation(fieldName, parameters)(sqlContext)
  }
}


private[sql] class KafkaRelation(val fieldName: String,
                                 var kafkaParams: Map[String, String])
                                (@transient val sqlContext: SQLContext)
  extends BaseRelation
    with TableScan
    with Serializable {

  //构建schma
  override def schema: StructType = StructType(
    Seq(
      StructField("key", StringType, false),
      StructField(fieldName, StringType, false)
    )
  )

  override def buildScan(): RDD[Row] = {
    val sparkContext = sqlContext.sparkContext
    val groupId = kafkaParams.getOrElse("group.id", sys.error("找不到消费组id"))
    val topticSet: Set[String] = kafkaParams.getOrElse("topics", sys.error("找不多topic")).split(",").toSet
    val maxRatePerPartition = kafkaParams.getOrElse("maxRatePerPartition", "100")
    val autoComitOffset: Boolean = kafkaParams.getOrElse("autoCommitOffset", "false").toBoolean

    //创建kafkamanager的实例,方便操作主题和分区的offset
    val kafkaManager = new KafkaManager(kafkaParams)

    val offsetByZkAndKafka = kafkaManager.getConsumerOffsetByZkAndKafka(groupId, topticSet)
    val offsetRange = offsetByZkAndKafka.map { case (tp, s) =>
      val endOffset = Math.min(s._1 + maxRatePerPartition.toInt, s._2)
      OffsetRange(tp, s._1, endOffset)
    }.toArray

    val defautRdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder](
      sparkContext,
      kafkaParams,
      offsetRange
    )

    val rddRow: RDD[Row] = defautRdd.map(line => {
      Row.fromSeq(Seq(if (null == line._1) "" else line._1, line._2))
    })

    if (autoComitOffset) {
      kafkaManager.saveConsumerOffset(topticSet.head, groupId, offsetRange)
    }

    rddRow
  }

}