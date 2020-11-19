package imooc.naga.server.query;


import imooc.naga.server.query.dataframe.DataFrame;

public interface ResultSetDataFrameWrapper<R> extends ResultSetWrapper<R, DataFrame> {

  public DataFrame wrapData(R result);
}
