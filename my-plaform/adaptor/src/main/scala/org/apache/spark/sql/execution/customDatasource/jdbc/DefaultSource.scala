/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.customDatasource.jdbc

import org.apache.spark.sql.execution.customDatasource.jdbc.JdbcUtils._
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, DataSourceRegister, RelationProvider}
import org.apache.spark.sql.{AnalysisException, DataFrame, SQLContext, SaveMode}

class DefaultSource extends CreatableRelationProvider
  with RelationProvider with DataSourceRegister {

  override def shortName(): String = "jdbc"

  override def createRelation(
      sqlContext: SQLContext,
      parameters: Map[String, String]): BaseRelation = {
    //封装用户写入的参数（url , driver信息 ， 表名 ， 用户名和密码等）
    val jdbcOptions = new JDBCOptions(parameters)
    //todo 如果不是mysql，比如hive，那么会有分区信息
    //分区key
    val partitionColumn = jdbcOptions.partitionColumn
    //查询下界
    val lowerBound = jdbcOptions.lowerBound
    //查询上界
    val upperBound = jdbcOptions.upperBound
    //分区数
    val numPartitions = jdbcOptions.numPartitions
    //如果分区信息为空 ， 则返回空
    val partitionInfo = if (partitionColumn == null) {
      null
    } else {//如果分区信息不为空，则返回JDBCPartitioningInfo信息
      JDBCPartitioningInfo(
        partitionColumn, lowerBound.toLong, upperBound.toLong, numPartitions.toInt)
    }
    //将分区信息数组化
    val parts = JDBCRelation.columnPartition(partitionInfo)
    //调用JDBCRelation（分区信息 ，封装用户写入的参数 ）（sparkSession）
    JDBCRelation(parts, jdbcOptions)(sqlContext.sparkSession)
  }

  override def createRelation(
                      sqlContext: SQLContext,
                      mode: SaveMode,
                      parameters: Map[String, String],
                      df: DataFrame): BaseRelation = {
    val jdbcOptions = new JDBCOptions(parameters)
    val url = jdbcOptions.url
    val table = jdbcOptions.table
    val createTableOptions = jdbcOptions.createTableOptions
    val isTruncate = jdbcOptions.isTruncate

    var saveMode = mode match{
      case SaveMode.Append =>CustomSaveMode.Append
      case SaveMode.Overwrite => CustomSaveMode.Overwrite
      case SaveMode.ErrorIfExists=>CustomSaveMode.ErrorIfExists
      case SaveMode.Ignore=>CustomSaveMode.Ignore
    }

    val parameterLower = parameters.map(line=>(line._1.toLowerCase(),line._2))
    if(parameterLower.keySet.contains("savemode")){
      saveMode = if(parameterLower("savemode").equals("update")) CustomSaveMode.Update else saveMode
    }

    val conn = JdbcUtils.createConnectionFactory(jdbcOptions)()
    try {
      val tableExists = JdbcUtils.tableExists(conn, url, table)
      val tableSchema = JdbcUtils.getSchemaOption(conn,jdbcOptions)

      if (tableExists) {
        saveMode match {
          case CustomSaveMode.Overwrite =>
            if (isTruncate && isCascadingTruncateTable(url) == Some(false)) {
              // In this case, we should truncate table and then load.
              truncateTable(conn, table)
              saveTable(df, url, table,tableSchema,saveMode, jdbcOptions)
            } else {
              // Otherwise, do not truncate the table, instead drop and recreate it
              dropTable(conn, table)
              createTable(df.schema, url, table, createTableOptions, conn)
              saveTable(df, url, table,tableSchema,saveMode,  jdbcOptions)
            }

          case CustomSaveMode.Append =>
            saveTable(df, url, table, tableSchema,saveMode, jdbcOptions)

          case CustomSaveMode.ErrorIfExists =>
            throw new AnalysisException(
              s"Table or view '$table' already exists. SaveMode: ErrorIfExists.")

          case CustomSaveMode.Ignore =>
            // With `SaveMode.Ignore` mode, if table already exists, the save operation is expected
            // to not save the contents of the DataFrame and to not change the existing data.
            // Therefore, it is okay to do nothing here and then just return the relation below.
          case CustomSaveMode.Update=>
            saveTable(df, url, table, tableSchema,saveMode, jdbcOptions)
        }
      } else {
        createTable(df.schema, url, table, createTableOptions, conn)
        saveTable(df, url, table,tableSchema,saveMode,  jdbcOptions)
      }
    } finally {
      conn.close()
    }

    createRelation(sqlContext, parameters)
  }
}
