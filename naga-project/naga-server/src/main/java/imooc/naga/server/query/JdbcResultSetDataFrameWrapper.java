package imooc.naga.server.query;

import imooc.naga.core.exception.ErrorCodes;
import imooc.naga.core.exception.NagaException;
import imooc.naga.server.query.cache.CacheThread;
import imooc.naga.server.query.cache.CacheThreadPool;
import imooc.naga.server.query.cache.DataFrameCache;
import imooc.naga.server.query.dataframe.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//实现:将我们的jdbc结果集resultset-->转化为Dataframe
public class JdbcResultSetDataFrameWrapper implements ResultSetDataFrameWrapper<ResultSet> {
    private Statement st = null; //用于执行静态SQL语句并返回其产生的结果的对象。
    private PreparedStatement ps = null; //表示预编译SQL语句的对象。
    private Connection connection = null;//与特定数据库的连接（会话）。在连接的上下文中执行SQL语句并返回结果
    private QueryObject queryObject = null;  //查询记录表

    public JdbcResultSetDataFrameWrapper setResources(QueryObject queryObject, Statement st, PreparedStatement ps, Connection connection) {
        //传入的相关数据,我们对其进行赋值
        this.st = st;
        this.ps = ps;
        this.connection = connection;
        this.queryObject = queryObject;
        return this;
    }
    //实现核心方法
    @Override
    public DataFrame wrapData(ResultSet result) {
        try {//获取到相关的元数据信息:检索此<code> ResultSet </ code>对象的列的数量，类型和属性
            ResultSetMetaData metaData = result.getMetaData();

            RowSchemaInfo.RowSchemaInfoBuilder rowSchemaInfoBuilder = RowSchemaInfo.newSchemaBuilder();
            int columnCount = metaData.getColumnCount(); //返回这个ResultSet对象中的列数。
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                DataType dataType = JdbcTypeMapping.getDataType(JDBCType.valueOf(metaData.getColumnType(i)));//jdbctype转化成我们等于的dataType
                rowSchemaInfoBuilder.column(columnName, dataType);//来构造我们dataframe
            }
            RowSchemaInfo rowSchemaInfo = rowSchemaInfoBuilder.build();
            DataFrameImpl dataFrame = new DataFrameImpl(rowSchemaInfo);
            fillDataFrame(result, rowSchemaInfo, dataFrame);
            return dataFrame;
        } catch (SQLException e) {
            throw new NagaException("sql exception" + e.getMessage(), ErrorCodes.SYSTEM_EXCEPTION);
        }
    }
    //遍历我们result,将遍历的值按照rowSchemaInfo放入我们dataFrameImpl
    private void fillDataFrame(ResultSet result, RowSchemaInfo rowSchemaInfo, DataFrameImpl dataFrame) throws SQLException {

        List<ResultSetColumnReader> readers = new ArrayList<>(rowSchemaInfo.getColumns().size());
        for (ColumnInfo columnInfo : rowSchemaInfo.getColumns()) {//datafrom列信息包含:名称,类型
            readers.add(JdbcTypeMapping.getResultSetColumnReader(columnInfo.getType()));//list存储的就是dataFram列信息
        }

        int num = 0;
        //这里是要进行分页的,将第一展示给用户,其他页要进行缓存
        PagedDataFrame pagedDataFrame = new PagedDataFrame(rowSchemaInfo, dataFrame);
        pagedDataFrame.setNowPageSize(queryObject.getPageSize());//设置一页的数据数量

        ////通过wehile循环获取一页数据
        while (result.next() && queryObject.getPageSize() > 0) {
            List<Object> values = new ArrayList<>(readers.size()); //构造一个list
            for (int i = 0; i < readers.size(); i++) { //循环遍历readers
                //readers.get(i)获取到这一列
                //通过readValue这个resultSet指定索引的值
                Object value = readers.get(i).readValue(i + 1, result);
                values.add(value); //将遍历的值存储到创建的list集合里面
            }
            dataFrame.append(values);//将values的行值,添加到构造的dataframe行值上
            if (++num >= queryObject.getPageSize() + 1) {
                pagedDataFrame.setRowCount(num);
                break;
            }
        }


        if (pagedDataFrame.getRowCount() == 0) {
            pagedDataFrame.setRowCount(num);
        }
        if(result.next()) {
            //todo 第一页1000条以外的数据,获取存储到缓存当中
            String unionStr = DataCacheUtil
                    .getUnionStrWithPid(queryObject.getCurrentUser(), queryObject.getSql());
            String uniqueKey = DataCacheUtil.generateMd5(unionStr);
            DataFrameCache.getDataFrameLoadingCache().put(uniqueKey, pagedDataFrame);
            CacheThread cacheThread = new CacheThread();
            cacheThread.setDataFrame(dataFrame);
            cacheThread.setPagedDataFrame(pagedDataFrame);
            cacheThread.setHexKey(uniqueKey);
            cacheThread.setConnection(connection);
            cacheThread.setStatement(st);
            cacheThread.setPreparedStatement(ps);
            cacheThread.setResultSet(result);
            cacheThread.setQueryObject(queryObject);
            CacheThreadPool.getInstance().execJob(cacheThread);
        }
    }
}
