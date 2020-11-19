package com.kkb.engine

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.concurrent.locks.ReentrantLock

import com.kkb.domain.engine.Job
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * Created by like
 */
class EngineSession(platEngine:String , _tag:Option[String]) {
  def engineInfo = platEngine

  def tag = _tag

  private val lock = new ReentrantLock()
  //锁通信
  private val condition = lock.newCondition()
  //是否退出线程等待状态
  private val stopped :Boolean = false
  private var throwable:Throwable = null

  /**保存批处理作业信息[引擎地址任务组ID，任务]*/
  val batchJob = new ConcurrentHashMap[String , Job]()

  /**让主线程等待子线程结束后在结束 ，否则可能出现僵尸进程*/
  def awaitTermination(timeout:Long = -1):Boolean = {
    lock.lock()
    try{

      if(timeout < 0){
        while (!stopped && throwable == null){
          condition.await()
        }
      }else{
        val nanos = TimeUnit.MILLISECONDS.toNanos(timeout)
        while (!stopped && throwable == null && nanos > 0){
          condition.awaitNanos(nanos)
        }
      }
      if(throwable != null){
        throw throwable
      }
      //如果线程走到这里，说明当前线程已经被停止了，或者任务超时
      stopped
    }finally {
      lock.unlock()
    }
  }

}

object EngineSession {
  def createSpark(sparkConf: SparkConf):SparkSession = {
    val spark = SparkSession
      .builder
      .appName("go")
      .config(sparkConf)
      //动态资源调整
      .config("spark.dynamicAllocation.enabled", "true")
      .config("spark.dynamicAllocation.executorIdleTimeout", "30s")
      .config("spark.dynamicAllocation.maxExecutors", "100")
      .config("spark.dynamicAllocation.minExecutors", "0")
      //动态分区
      .config("hive.exec.dynamic.partition", "true")
      .config("hive.exec.dynamic.partition.mode", "nonstrict")
      .config("hive.exec.max.dynamic.partitions", 20000)
      //调度模式
      .config("spark.scheduler.mode", "FAIR")
      .config("spark.executor.memoryOverhead", "512") //堆外内存
      //序列化
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

      //es
      .config("es.index.auto.create","true") //在spark中自动创建es中的索引
      .config("es.nodes","node01")//设置在spark中连接es的url和端口
      .config("es.port","9200")
      /**
       * es.nodes.wan.only设置为true时即只通过client节点进行读取操作，因此主节点负载会特别高，性能很差。
       * 长时间运行后，java gc回收一次要几十秒，慢慢的OOM，系统崩溃。
       * */
      .config("es.nodes.wan.only","false")

      .master("local[*]")
      //启动spark sql支持hive，能够和hive做集成，这样做之后我们就可以通过sparksqlcontext穿件hivesqlcontext对象
//      .enableHiveSupport()//sqlcontext.sql()
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    spark
  }
}
