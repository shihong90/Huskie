package com.kkb.engine

import java.util.concurrent.ConcurrentHashMap

import com.cartravel.engine.antlr.EngineBaseListener
import com.cartravel.engine.antlr.EngineParser.{OverwriteContext, SqlContext}
import com.kkb.engine.adaptor.DML.{ExplainAdaptor, LoadAdaptor, SaveAdaptor, SelectAdaptor}
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.collection.mutable

class EngineSQLExecListener(_sparkSession: SparkSession) extends EngineBaseListener {
  var mode = ""
  var sparkSession = _sparkSession
  //保存结果，方便通过监听器对象获取可以返回到客户端
  private val _result = new ConcurrentHashMap[String, String]

  private val _env = new mutable.HashMap[String, String]()

  def addResult(k: String, v: String) = {
    _result.put(k, v)
    this
  }

  def addEnv(k: String, v: String) = {
    _env(k) = v
    this
  }

  def env() = _env

  def getResult(k: String) = {
    _result.getOrDefault(k, "")
  }

  def result() = _result

  override def exitSql(ctx: SqlContext): Unit = {
    /**
      *
      */
    ctx.getChild(0).getText().toLowerCase() match {

      case "load" => {
        println("load操作！")
        new LoadAdaptor(this).parse(ctx)
      }
      case "select" =>
        println("select操作！")
        new SelectAdaptor(this).parse(ctx)
      case "save" =>
        new SaveAdaptor(this).parse(ctx)
      case "create" =>
      case "insert" =>
      case "drop" =>
      case "truncate" =>
      case "show" =>
      case "explain" =>
        new ExplainAdaptor(this).parse(ctx)
      case "include" =>
    }
  }
}
