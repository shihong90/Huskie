package imooc.naga.server.query;

public interface ResultSetWrapper<R, T> {

  public T wrapData(R result);

}
