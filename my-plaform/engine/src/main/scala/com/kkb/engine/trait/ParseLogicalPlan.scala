package com.kkb.engine.`trait`

import com.cartravel.engine.antlr.EngineParser.SqlContext

trait ParseLogicalPlan {
  def parse(ctx: SqlContext): Unit
}

trait ParseLogicalTools {
  def cleanStr(str: String): String = {
    str match {
      //就是把输入的内容```select * from tb;```的字符串中的select * from tb;截取出来
      case x if x.startsWith("```") && x.endsWith("```") => x.substring(3, x.length - 3)
      case x if x.startsWith("'")||x.startsWith("\"")||x.startsWith("`")=>x.substring(1,x.length-1)
      case _=>str
    }
  }
}
