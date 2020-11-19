package org.apache.spark.sql.execution.customDatasource.kafka

import kafka.common.TopicAndPartition
import org.apache.spark.SparkException
import org.apache.spark.streaming.kafka.{KafkaCluster, OffsetRange}
import org.apache.spark.streaming.kafka.KafkaCluster.Err

class KafkaManager(kafkaParams: Map[String, String]) extends Serializable {
  lazy val kc = new KafkaCluster(kafkaParams)

  def getTopicAndPartitionOffset(topics: Set[String],
                                 isSmallest: Boolean = true): Map[TopicAndPartition, Long] = {
    /**
      * scala中方的Either就是类似java当中的try catch操作
      * 对java中的try catch机制做了增强
      * 类似Option
      */
    val errOrPartitionToLong: Either[Err, Map[TopicAndPartition, Long]] = for {
      //拿到每个分区的区号
      topicPartitions <- kc.getPartitions(topics).right
      //判断是否按照earlist方式获取offset
      leaderOffsets <- (if (isSmallest) {
        kc.getEarliestLeaderOffsets(topicPartitions)
      } else {
        kc.getLatestLeaderOffsets(topicPartitions)
      }).right
    } yield {
      /**
        * for yield是一个组合操作语法：就是每一次for循环遍历的结果，通过yield记录下来，最后形成新的集合
        */
      leaderOffsets.map { case (tp, lo) => (tp, lo.offset) }
    }

    errOrPartitionToLong.fold(
      errs => throw new SparkException(errs.mkString(",")),
      ok => ok
    )

  }

  //获取分区最大和最小便宜量
  def getClusterOffsetRangs(topics: Set[String]): Iterable[(String, Int, Long, Long)] = {
    val isSmallest = false;
    //最大偏移量
    val largest: Map[TopicAndPartition, Long] = getTopicAndPartitionOffset(topics, isSmallest)
    //最小偏移量
    val smallest: Map[TopicAndPartition, Long] = getTopicAndPartitionOffset(topics)
    val topicPartitionMap2topicOffset = (topicaPartition: Map[TopicAndPartition, Long]) => {
      val tupleToLong = topicaPartition.map {
        case (k, v) =>
          (k.topic, k.partition) -> v
      }
      tupleToLong
    }

    val largestMap: Map[(String, Int), Long] = topicPartitionMap2topicOffset(largest)
    val smallMap: Map[(String, Int), Long] = topicPartitionMap2topicOffset(smallest)

    for {
      small <- smallMap
      large <- largestMap
      if small._1 == large._1
      //主题，分区号，最小偏移量，最大偏移量
    } yield (small._1._1, small._1._2, small._2, large._2)
  }

  def getConsumerOffsetByZkAndKafka(groupId: String, topicSet: Set[String]): Map[TopicAndPartition, (Long, Long)] = {
    var fromOffsets: Map[TopicAndPartition, (Long, Long)] = Map[TopicAndPartition, (Long, Long)]()
    var topicAndPartition = kc.getPartitions(topicSet).right.get
    val consumerOffsetFromZK = kc.getConsumerOffsets(groupId, topicAndPartition)
    val tuples = getClusterOffsetRangs(topicSet)

    if (consumerOffsetFromZK.isLeft) {
      for {
        tp <- topicAndPartition
        (topic, partitionId, smallestOffset, largestOffset) <- tuples
        if (tp.topic == topic && tp.partition == partitionId)
      } fromOffsets += (tp -> (smallestOffset, largestOffset))
    } else {
      for {
        (tp, offset) <- consumerOffsetFromZK.right.get
        (topic, partitionId, smallestOffset, largestOffset) <- tuples
        if (tp.topic == topic && tp.partition == partitionId)
      } fromOffsets += (tp -> (Math.max(offset, smallestOffset), largestOffset))
    }
    fromOffsets
  }


  def saveConsumerOffset(
                          topic: String,
                          groupId: String,
                          offsetRanges: Array[OffsetRange]
                        ): Either[Err, Map[TopicAndPartition, Short]] = {
    var offsets: Map[TopicAndPartition, Long] = Map()
    for (o <- offsetRanges) {
      offsets += (TopicAndPartition(topic, o.partition) -> o.untilOffset)
    }
    kc.setConsumerOffsets(groupId, offsets)
  }

}
