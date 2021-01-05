##### 什么是搜索? 

通过一个关键词或一段描述,得到你想要的结果

##### 如何实现搜索功能?



##### 如果使用关系型数据库做索引,他会带来哪些问题?

性能差,不可靠,相关度很低

##### 倒排索引、Lucene和全文检索?

数据结构1.包含这个关键词的document list

2.关键词在每个doc中出现的次数TF term frequency

3.关键词在整个索引中出现的次数IDF inverse doc frequency

IDF越高也就是相关的低

4.关键词在当前doc中出现的次数

5.每个doc的长度,越长相关度越低

6.包含这个关键词的所有doc的平均长度

Luncenr:jar包,帮我们创建倒排索引,提供了复杂的API

##### Luncenr做集群会有那些问题?

- 1.节点一旦宕机,数据丢失,后果不堪设想,可用性差

- 2.自己维护,单台节点的承载请求的能力是有限的,需要人工作负载


##### Elastisearch和搜索引擎能否化等号?

ES不光是搜索引擎是存储和数据分析引擎,他有非常强大的聚合分析能力

##### ES的核心数据结构json和元数据document

##### 索引分片

##### ES有哪些优势?

- 1.面向开发者优化,屏蔽了Lucene的复杂特性,集群自动发现

- 2.自动维护数据在多个节点上的建立

- 3.会帮我们做搜索请求的负载均衡

- 4.自动维护冗余副本,保证了部分节点宕机的情况下任然不会有数据丢失

- 5.ES基于Lucene提供了很多高级功能:符合查询,聚合分析,基于地理位置

- 6.对应大公司可以构建几百台服务器大型分布式集群,处理PB级别数据

- 7.相对于传统数据,提供了全文检索,同义词处理,相关度排名.聚合分析以及

  海量数据近实时处理

#### ES的应用领域都有哪些?

百度:全文检索,高亮,搜索推荐

各大网站的用户行为日志

BI商业智能,数据分析,数据挖掘统计

ELK:Elasticsearch(存储),Logstash(日志采集),Kibana(可视化)

#### ES集群是怎样实现高可用?

- 1.ES在分配单个索引的分片时会将每个分片尽可能分配到更多的节点上.但是,实际情况取决于集群拥有的分片和索引的数量以及它们的大小,不一定总是能均匀的分布

- 2.ES不允许Primary和它的Relica放在同一个节点中,并且同一个节点不接受完全相同的两个Replica

- 3.同一个节点允许多个索引的分片同时存在

- 4.三台节点:3个Primary两个副本共6个Replica,此时,在不考虑master选举的情况下允许宕机两个节点,能承载的最大容量是6T,集群最大支持的QPS:3000

  为了最大程度保证数据的可用性,ES让每个节点尽量分配完整的0/1/2三个不同的分片,保证数据的完整性,这样,任何一台机器都能找到完整的数据,从而实现高可用.

#### 啥叫容错?

- 1.向下兼容
- 2.在局部出错异常的情况下,保证服务政策允许并且有自行恢复的能力

#### ES的容错是如何实现的?



#### ES-node

- Master:主节点,每个集群都有且只有一个
  - 尽量避免Master节点这样设置node.data=true
- voting:投票节点
  - 默认是数据节点就有多少投票节点就有多少
  - Node.voting_only(仅投票,即使配置了data.master=true,也不会参选,但是任然可以作为数据节点)
- coordinating:协调节点
  - 每一个节点都隐式的是一个协调节点,如果同时设置了data.master=false和data.data=false,name此节点将成为仅协调节点

- Master-eligible node:候选节点
- Data node:数据节点,就是专门存储数据的
- Ingest node:
- Machine learning node:机器学习节点

#### node.master和node.data配置

- 1.node.master=true  node.data=true

  这是ES节点默认配置,既作为候选节点又作为数据节点,这样的节点一旦被选举为Master,压力是比较大的,通常来说Master节点应该只承担较为轻量级的任务,比如创建删除索引,分片均衡等

- 2.node.master=true  node.data=false

  只作为候选节点,不作为数据节点,课参选Master节点,当选后成为真正的master节点

- 3.node.master=false  node.data=false

  即不当候选节点,也不作为数据节点,那就是仅协调节点,负责负载均衡

- 4.node.master=flase  node.data=true

  不作为候选节点,但是作为数据节点,这样的节点主要负责数据存储和查询服务

#### 深度剖析Elasticsearch分布式架构的原理

1. 

#### ES的容错机制以及如何实现高可用

第一步:Master选举

- 脑裂:可能会产生多个Master节点
- 配置:discovery.zen.minimum_master_nodes=N/2+1

第二步:Replica容错,新的或者原有的Master节点会将丢失的Primary对应的某个副本提升为Primary

第三步:新Master重启故障机

第四步:数据同步,Master会将宕机期间丢失的数据同步到重启机器对应的分片上去 

#### Master选举的流程是什么?

每一个节点都会在while循环中去判断当前节点是否是null,如果一旦是null就会调用findMaster方法,该方法会ping所有节点,看看当前集群有没有Master节点,如果有并且是自己就会临时当选Master,如果没有并且自己也不Master以及minimum_master_node个节点都联系不上Master确定Master失联或者GG就会进入Master选举,从候选节点(node.master:true)中选举Master,投票会有以下几个过程:

- 1.寻找clusterStateVersion比自己高的候选节点,向其发送选票
- 2.如果clusterStartVersion一样,则在候选节点(包含当前节点)中选id最小的一个节点,向该节点发送选举投票
- 3.如果一个节点收到足够多的投票并且它也向自己投票,那么该节点将成为master节点开始发布集群状态

#### 脑裂

两台节点通信短路,产生脑裂,当通信恢复时就发现两个master节点.

解决方式:通过配置discovery.zen.minmum_master_nodes=N/2+1

选举出一台master节点最少需求n/2+1个投票节点

#### 集群健康值检查

```http
//通过这个url就可以查询集群的健康状况
loaclhost:9200/_cluster/health
```

```java
ES集群检查插件安装:
https://nodejs.orh/en/download/  node-v
npm install -g grunt-cli   grunt-version
https://github.com/mobz/elasticsearch-head
改glasticsearch-head-master\Grunfile.js  connect/server/options
/增加:hostname:'*',
npm install   npm run start

默认端口是9100
```

- 1.Green:所有p shard和r shard均为active,集群很健康

- 2.Yellow:至少一个replica shard不可以,但是数据仍然是完整的

- 3.Red:至少有一个p shard为不可用状态,数据不完整,集群不可用


#### ES的核心概念

- 1.集群Cluster:每个集群都至少包含两个节点,1个节点ES也能跑
- 2.Node:集群中的每个节点,一个节点不代表一个服务器,ES在一台机器上可以运行多个节点
- 3.Field:一个数据字段,与index和type一起,可以定义一个doc,我们可以收到指定doc的id
- 4.Document:是json的最小的数据单元,可以理解为行
- 5.Type:一个索引通常会被划分成多个Type,逻辑上的数据分类

- 6.Index:一类相同或者类型的doc,比如一个员工索引,商品索引

- Doc 等价于row;type等价于table;index 等价于db

- Shard分片:
  - 1.一个index包含多个Shard,默认5P默认每个P分配一个R,P的数量在创建索引的时候设置,如果想要修改,需要重建索引
  - 2.每个Shard都是一个Luncene实例,有完整的创建索引的处理请求能力
  - 3.ES会自动在nodes上为我们做shard均衡
  - 4.一个doc是不可能同时存在于多个PShard中的,但是可以存在于多个RShard中
  - 5.P和对应的R不能同时存在于同一个节点,所以最低的可用配置是两台节点,互为主备

Pashard可读可写

RShard只读的(副本)

#### ES的CRUD

- 1.创建索引:PUT/索引名?pretty   

  - 注意实现：索引名字一律小写字母

- 2.删除索引:DELETE/test_index?pretty

- 3.插入数据:PUT/index/_doc/id

- ```java
  {
      Jon数据
  }
  ```

- 4.更新/修改数据

  - 4.1.全量替换

```java
PUT /product/_doc/1
{
    "name" : "xiaomi phone",
    "desc" : "shouji zhang",
    "price" : 13999
        
}
```

- 4.2指定字段更新

```java
POST /index/_doc/id/_update
{
	"doc"：{
		"field":value
	}
}
```

- 5删除数据

```java
DELETE/index/_doc/id
```

- 6.查询数据

```java
GET /product/_doc/_search
```

- 查询返回的Json所包含内容
  - took:这次请求的毫秒数
  - time_out:是否超时
  - total:分片数量
  - sucessful:成功了多少个
  - skipped:跳过了多少个
  - failed:失败了多少个
  - hits:匹配记录
  - values:结果数量
  - max_score:最大的相关度分数

#### 如何提高ES分布式系统的可用性以及性能最大化

- 1.每台节点的Shard数量越少,每个shard分配的cpu、内存和IO的资源越多,单个shard的性能越好,当一台机器一个shard时,单个shard性能最好

- 2.稳定的master节点对于集群健康非常重要!理论上讲,应该尽可能的减轻master节点的压力,分片数量越多,master节点维护管理shard的任务越重,并且节点可能就要承担更多的数据转发任务,可增加"仅协调"节点来缓解Master节点和Data节点的压力,但是在集群中添加过多的"仅协调"会增加集群负担,因为选择的主节点必须等待每个节点的集群状态更新确认.

- 3.反过来说,如果相同资源分配相同的前提下,shard数量越少,单个shard的体积越大,查询性能越低,速度越慢,这个取舍应根据实际集群状况和结合应用场景等因素综合考虑

- 4.数据节点和Master节点一定要分开,集群规模越大,这样做的意义也就越大

- 5.数据节点处理与数据相关操作,例如CRUD,搜索和聚合.这些操作是I/O,内存和CPU密集型的,所以他们需要更高配置的服务器以及更高的宽带,并且集群的性能冗余非常重要
- 6.由于仅投票节点不参与Master竞选,所以和真正的master节点相比,他需要的内存和cpu较少.但是,所有候选节点以及投票节点都可能是数据节点,所以他们都需要快速稳定低延迟的网络
- 7.高可用性集群至少需要三个主节点,其中至少两个不是仅投票节点.即使其中一个节点发生故障,这样的集群也能够选举一个主节点.生成环境最好设置3台仅Master候选节点(nde.master=true  node.data=true)

#### Searchtimeout

- 1.设置:默认是没有timeout,如果设置了timeout,那么执行timeout机制

- 2.timeout机制:假设用户查询结果有1w条数据,但需要10s才能完毕,但是yoghurt设置了1s的timeout,那么不管当前一共查询到了多少数据,都会在1s后ES将停止查询,并返回当前数据

```JAVA
GET /_search?timeout=1s  //设置当前的超时时间
```

#### ES常用查询

ES 7已经抛弃了_doc

- 1.Query_string
  - 查询所有GET /product/_search
  - 带参数GET /product/_search?q=name:xiaomi

分页

```java
分页GET /product/_search?from=0&size=2$sort=price:asc
from 页码
size 是容量
GET /product/_search
{
	"query":{
	 	"match_all":{}
	},
	"from": 0,
	"size": 2
}
```

match_all

```java
GET /product/_search
{
	"query":{
	 	"match_all":{}  //匹配所有
	}
}
```

match

```java

GET /product/_search
{
	"query":{ //query是查询
    	"match":{ //match是匹配
    		"name":"nfc"  //条件:name字段中包含nfc
    	}
	}
}
```

排序

```java
GET /product/_search
{
	"query":{ 
    	"match":{
    		"name":"nfc"
    	}
	},
    "sort":[ //排序
      {
      	"price": "desc"	 //排序规则 
      }
    ]
}
```

multi_match

```java
GET /product/_search
{
	"query":{ 
    	"multi_match":{ //多字段匹配
    		"query":"nfc",
    		"fields":["name","desc"] //匹配出name和desc字段中要包含nfc的数据
    	}
	},
    "sort":[
      {
      	"price": "desc"	 
      }
    ]
}
```

_source

```java
GET /product/_search
{
	"query":{
	 	"match_all":{}
	},
	"_source": ["name","price"] //通过限定_source的字段,返回数据仅仅会显示锁限定字段
}

```

#### Full-text queries:全文检索

- query-term:不会被分词

```java
GET /product/_search
{
	"query":{ 
    	"term":{ //查询条件"nfc"不会再进行分词
    		"name":"nfc"
    	}
	}
}
```



```java
#等同于: select ...where name in("")
GET /product/_search
{ 
	"query": {
    	"terms": {
    		"name": ["nfc","phone"] //name包含nfc,phone
    	}
	}
}
```



- 全文检索
  - Phrase search:短语搜索

```java
GET /product/_search
{
	"query": {
    	"match_phrase": {
    		"name": "xiaomi nfc zhineng phone"
    	}
	}
}
```

- Query and filter:查询和过滤	

  - 1.bool可以组合多个查询条件,bool查询也是采用more_matches_is_better的机制,因此满足must和should子句的文档将会合并起来计算分值
    - 1.must:必须满足
    - 2.filter:过滤器 不计算相关度分析
    - 3.should:可能满足,类似于sql的or
    - 4.must_not:必须不满足 不计算相关度分数

  

```java

```



#### 延迟删除特性