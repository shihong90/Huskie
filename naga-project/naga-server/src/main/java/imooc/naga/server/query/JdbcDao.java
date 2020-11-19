package imooc.naga.server.query;

import imooc.naga.core.exception.ErrorCodes;
import imooc.naga.core.exception.NagaException;
import imooc.naga.server.query.dataframe.DataFrame;

import java.sql.*;
//有两个方法:
//  1.查询,返回我们的datafrom
//  2.查询,返回resultset
public class JdbcDao {
    //查询 返回 dataframe
    public static DataFrame queryAsDataFrame(QueryObject jdbcQo, Connection conn) {
        //生成dataframe就是表中的jdbc操作
        Statement statement = null; //用个执行sql语句的对象
        PreparedStatement preparedStatement = null; //表示预编译SQL语句的对象。
        ResultSet resultSet = null; //表示数据库结果集的数据表
        try {
            if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("presto")) {
                statement = conn.createStatement();
                resultSet = statement.executeQuery(jdbcQo.getSql());
            } else {
                preparedStatement = conn.prepareStatement(jdbcQo.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = preparedStatement.executeQuery();
            }
            //将resultset 转换为dataframe
            return getResultWrapper().setResources(jdbcQo, statement, preparedStatement, conn).wrapData(resultSet);
        } catch (SQLException e) {
            throw new NagaException("query error" + e.getMessage(), ErrorCodes.SYSTEM_EXCEPTION);
        }
    }


    //查询返回resultset
    public static ResultSet query(QueryObject jdbcQo, Connection conn) {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("presto")) {
                statement = conn.createStatement();
                resultSet = statement.executeQuery(jdbcQo.getSql());
            } else {
                preparedStatement = conn.prepareStatement(jdbcQo.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = preparedStatement.executeQuery();
            }
            return resultSet;
        } catch (SQLException e) {
            throw new NagaException("query error" + e.getMessage(), ErrorCodes.SYSTEM_EXCEPTION);
        }
    }

    private static JdbcResultSetDataFrameWrapper getResultWrapper() {
        return new JdbcResultSetDataFrameWrapper();
    }
}
