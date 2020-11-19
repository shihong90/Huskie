package imooc.naga.server.query.dataframe;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataFrame extends Iterable<Row> {
  //获取元数据信息
  public DataFrameMetaData getMetaData();
  //切片
  public DataFrame slice(int startRow, int endRow);
  //行的数量
  public int rowCount();
  //列的数量
  public int columnCount();
  //查询指定列
  public DataFrame selectColumns(String[] columns);
  //删除指定的列
  public DataFrame removeColumns(String[] columns);

  public List<Row> head(int count);

  public List<Row> tail(int count);
  //合并Dataframe
  public DataFrame unionAll(List<DataFrame> others);
  //添加行值
  public void append(List<Object> rowValues);
  //重载方法:添加行值
  public void append(Object[] rowValues);
  //序列化
  public void serializeTo(OutputStream outputStream, DataFrameSerializer serializer);
  //获取通过名称获取列
  public Column getColumn(String name);
  //获取dataFrame的构造schema
  public RowSchemaInfo getRowSchemaInfo();
  //转置
  public Map<String, Object[]> transpose();
  //合计
  public DataFrame aggregate(String[] aggrColumns, AggregateFuction[] fuctions,
                             String... groupColumns);

  public DataFrame distinct(String[] columns);


}
