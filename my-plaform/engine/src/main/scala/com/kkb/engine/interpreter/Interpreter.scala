package com.kkb.engine.interpreter

import org.apache.spark.SparkConf
import org.json4s.JsonAST.JObject

/**
 * 解析引擎(顶层的解析任务状态管理)
 */
object Interpreter {

  //目的把引擎的所有的响应统一管理,上层的抽象
  abstract class ExecuteResponse

  //执行成功的任务
  case class ExecuteSuccess(content: JObject) extends ExecuteResponse

  //执行失败的任务
  case class ExecuteError(
                           executeName: String, //任务名称
                           executeValue: String, //任务失败原因
                           trackback: Seq[String] //失败的堆栈信息(类似java代码中的异常信息，一行一行的)
                         ) extends ExecuteResponse

  //为未完成的任务
  case class ExecuteIncomplete() extends ExecuteResponse

  //终止的任务
  case class ExecuteAborted(message: String) extends ExecuteResponse

}

/**
 * 对外提供引擎的方法
 */
trait Interpreter {

  import Interpreter._

  /**
   * 对外提供的启动方法
   *
   * @return
   */
  def start(): SparkConf

  /**
   * 关闭引擎
   */
  def close(): Unit

  /**
   * 命令执行方法(代码/SQL),为了保证安全，包内可见
   *
   * @param order
   * @return
   */
  private[interpreter] def execute(order: String): ExecuteResponse
}
