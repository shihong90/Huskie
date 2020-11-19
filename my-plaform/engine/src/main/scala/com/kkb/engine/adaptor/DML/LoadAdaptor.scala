package com.kkb.engine.adaptor.DML

import com.cartravel.engine.antlr.EngineParser
import com.cartravel.engine.antlr.EngineParser.{BooleanExpressionContext, ExpressionContext, FormatContext, PathContext, TableNameContext}
import com.kkb.engine.EngineSQLExecListener
import com.kkb.engine.`trait`.{ParseLogicalPlan, ParseLogicalTools}
import com.kkb.engine.adaptor.BatchJobLoadAdpator

class LoadAdaptor(engineSQLExecListener: EngineSQLExecListener) extends ParseLogicalPlan  {
  //代表我们将要操作的数据类型(Text,jdbc,csv,json)
  var format = ""
  var path = ""
  var tableName = ""
  var option = Map[String, String]()

  //('load'|'LOAD') format '.'? path ('where' | 'WHERE')? expression? booleanExpression*  'as' tableName
  override def parse(ctx: EngineParser.SqlContext): Unit = {
    println("childCount:"+ctx.getChildCount)
    println("childCount1:"+ctx.getChild(1))
    println("childCount2:"+ctx.getChild(2))
    println("childCount3:"+ctx.getChild(3))
    println("childCount4:"+ctx.getChild(4))

    (0 until ctx.getChildCount).foreach(tokenIndex => {
      ctx.getChild(tokenIndex) match {
        case s: FormatContext => {
          format = s.getText
        }
        case s: ExpressionContext => { //匹配where后面的表达式内容
          val identifier = cleanStr(s.identifier().getText)
          val valueStr = cleanStr(s.STRING().getText)
          option += (identifier -> valueStr)
        }
        case s: PathContext => {
          path = cleanStr(s.getText)
        }
        case s: BooleanExpressionContext => {
          //ps = 100;
          option += (cleanStr(s.expression().identifier().getText) -> cleanStr(s.expression().STRING().getText))
        }
        case s: TableNameContext => {
          tableName = s.getText
        }
        case _=>
      }
    })

    //判断spark的job是流式还是离线的
    if (option.contains("spark.job.mode") && option("spark.job.mode").equals("stream")) {
      engineSQLExecListener.addEnv("stream","true")
      println(s"流式处理:format=${format},path:${path},tableName:${tableName}")
    } else {
      println(s"离线处理:format=${format},path:${path},tableName:${tableName}")
      new BatchJobLoadAdpator(engineSQLExecListener,format,path,tableName,option).parse
    }
  }

  def cleanStr(str: String): String = {
    str match {
      //就是把输入的内容```select * from tb;```的字符串中的select * from tb;截取出来
      case x if x.startsWith("```") && x.endsWith("```") => x.substring(3, x.length - 3)
      case x if x.startsWith("'")||x.startsWith("\"")||x.startsWith("`")=>x.substring(1,x.length-1)
      case _=>str
    }
  }
}
