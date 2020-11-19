package com.kkb.engine.adaptor.DML

import com.cartravel.engine.antlr.EngineParser
import com.cartravel.engine.antlr.EngineParser.{AppendContext, BooleanExpressionContext, ColContext, ErrorIfExistsContext, ExpressionContext, FormatContext, IgnoreContext, NumPartitionContext, OverwriteContext, PathContext, TableNameContext, UpdateContext}
import com.kkb.engine.EngineSQLExecListener
import com.kkb.engine.`trait`.{ParseLogicalPlan, ParseLogicalTools}
import com.kkb.engine.adaptor.BatchJobSaveAdaptor
import org.apache.spark.sql.{DataFrame, SaveMode}

class SaveAdaptor(engineSQLExecListener: EngineSQLExecListener)
  extends ParseLogicalPlan
    with ParseLogicalTools {
  var data: DataFrame = null
  var mode = SaveMode.ErrorIfExists
  //文件系统路径
  var save_path = ""
  //存储数据的引擎:hdfs,hbase,redis
  var format = ""
  var option = Map[String, String]()
  var tableName: String = ""
  var partitionByCol = Array[String]()
  var numPartition: Int = 1

  override def parse(ctx: EngineParser.SqlContext): Unit = {
    (0 until ctx.getChildCount).foreach(tokenIndex => {
      ctx.getChild(tokenIndex) match {
        case tag: OverwriteContext => mode = SaveMode.Overwrite
        case tag: AppendContext => mode = SaveMode.Append
        case tag: ErrorIfExistsContext => mode = SaveMode.ErrorIfExists
        case tag: IgnoreContext => mode = SaveMode.Ignore
        //spark里面目前不支持update操作，后续我们将要实现让spark支持update
        case tag: UpdateContext => option += ("savemode" -> "update")
        case tag: TableNameContext => {
          tableName = tag.getText
          //DataFrame注册到sparkSession临时试图中，才可以这样获取
          data = engineSQLExecListener.sparkSession.table(tableName)
        }
        case tag: FormatContext =>
          format = tag.getText
        case tag: PathContext =>
          save_path = cleanStr(tag.getText)
        //where关键字后面的条件获取方式
        case tag: ExpressionContext =>
          option += (cleanStr(tag.identifier().getText) -> cleanStr(tag.STRING().getText))
        //select * from where name=? and age=?
        case tag: BooleanExpressionContext =>
          option += (cleanStr(tag.expression().identifier().getText) -> cleanStr(tag.expression().STRING().getText))
        case tag: ColContext =>
          partitionByCol = cleanStr(tag.getText).split(",")
        case tag:NumPartitionContext=>
          numPartition = tag.getText.toInt
        case _=>
      }
    })


    //流处理和批处理的判断
    if(engineSQLExecListener.env().contains("stream")){
      //流处理的save操作

    }else{
      //批处理的save操作
      new BatchJobSaveAdaptor(
        engineSQLExecListener,
        data,
        save_path,
        tableName,
        format,
        mode,
        partitionByCol,
        numPartition,
        option
      ).parse
    }

  }
}
