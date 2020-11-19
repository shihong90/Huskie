package imooc.naga.server.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//数据功能是这样的接收用户请求,然后通过presto进行数据查询,查询数据后返回给用户,但是我们的场景是大数据
//需要对查询出来的数据进行缓存,对sql查询出来的数据优先进行分页,第一页发送给用户,其余缓存到redis里面
//用户进行分页的过程中是从redis里面进行获取的


//查询记录表
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryObject {
    private String sql;
    private int pageSize = DataCacheUtil.PageSize; //默认是一页1000条数据
    private String currentUser;
    private EngineType engineType;
}
