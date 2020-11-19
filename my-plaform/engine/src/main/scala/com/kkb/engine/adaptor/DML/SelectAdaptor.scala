package com.kkb.engine.adaptor.DML

import java.util.UUID

import com.cartravel.engine.antlr.{EngineLexer, EngineParser}
import com.kkb.engine.EngineSQLExecListener
import com.kkb.engine.`trait`.ParseLogicalPlan
import org.antlr.v4.runtime.misc.Interval

class SelectAdaptor(engineSQLExecListener: EngineSQLExecListener) extends ParseLogicalPlan{
  override def parse(ctx: EngineParser.SqlContext): Unit = {

    val sparkSession = engineSQLExecListener.sparkSession

    // load xxx from as xx;\n select * from xx;
    //首先需要从输入中获取到select语句
    //_input 代表输入的一整行sql(load xxx from as xx;\n select * from xx;)
    val input = ctx.start.getTokenSource.asInstanceOf[EngineLexer]._input
    val start = ctx.start.getStartIndex
    val stop = ctx.stop.getStopIndex
    val interval = new Interval(start,stop)
    //select * from xx;
    val originalText = input.getText(interval)

    //AAA-AAA-BBB-AAAAAA(感觉不好，看着像是常量或者xxx秘钥，所以把-替换掉)
    val tmpTable = UUID.randomUUID().toString.replace("-","")
    sparkSession.sql(originalText).createOrReplaceTempView(tmpTable)
    engineSQLExecListener.addResult("tmpTable",tmpTable)
    engineSQLExecListener.sparkSession.sql(s"select *  from ${tmpTable}").show()
  }
}
