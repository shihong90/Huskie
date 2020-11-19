package com.kkb.engine.adaptor.DML

import com.cartravel.engine.antlr.{EngineLexer, EngineParser}
import com.kkb.engine.EngineSQLExecListener
import com.kkb.engine.`trait`.{ParseLogicalPlan, ParseLogicalTools}
import org.antlr.v4.runtime.misc.Interval

class ExplainAdaptor(engineSQLExecListener: EngineSQLExecListener)
  extends ParseLogicalPlan with ParseLogicalTools {
  override def parse(ctx: EngineParser.SqlContext): Unit = {
    val sparkSession = engineSQLExecListener.sparkSession
    val input = ctx.start.getTokenSource.asInstanceOf[EngineLexer]._input
    //sql语句在语法树中的开始位置
    val start = ctx.start.getStartIndex
    //sql语句在语法树中的结束位置
    val stop = ctx.stop.getStopIndex

    //有点类似我们截取字符串
    val interval = new Interval(start,stop)
    val originalText = input.getText(interval)
    val chunks:Array[String] = originalText.replace(";","").split("\\s")

    chunks.length match{
      case 2=>
        engineSQLExecListener.addResult("explainStr",sparkSession.table(chunks(1)).queryExecution.toString())
      case _=>
        engineSQLExecListener.addResult("explainStr",sparkSession.sql(chunks.tail.mkString(" ")).queryExecution.toString())
    }

  }
}
