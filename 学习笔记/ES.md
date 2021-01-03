老虎证券QQ:869893807 

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

​	1.节点一旦宕机,数据丢失,后果不堪设想,可用性差

​	2.自己维护,单台节点的承载请求的能力是有限的,需要人工作负载

##### Elastisearch和搜索引擎能否化等号?

ES不光是搜索引擎是存储和数据分析引擎,他有非常强大的聚合分析能力

##### ES的核心数据结构json和元数据document

##### 索引分片

##### ES有哪些优势?

1.面向开发者优化,屏蔽了Lucene的复杂特性,集群自动发现

2.自动维护数据在多个节点上的建立

3.会帮我们做搜索请求的负载均衡

4.自动维护冗余副本,保证了部分节点宕机的情况下任然不会有数据丢失

5.ES基于Lucene提供了很多高级功能:符合查询,聚合分析,基于地理位置

6.对应大公司可以构建几百台服务器大型分布式集群,处理PB级别数据

7.相对于传统数据,提供了全文检索,同义词处理,相关度排名.聚合分析以及

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

#### ES的容错是如何实现的?



#### ES-node

- 1.Role 
  - Master:主节点
  - voting:投票
  - coordinating:仅协调
- 2.Node-type
  - Master-eligible node:候选节点
  - Data node:数据节点,就是专门存储数据的
  - Ingest nide:
  - Machine learning node:机器学习节点
- Master-eligible

#### 深度剖析Elasticsearch分布式架构的原理

1. 

#### ES的容错机制以及如何实现高可用

第一步:Master选举

- 脑裂:可能会产生多个Master节点
- 配置:discovery.zen.minimum_master_nodes=N/2+1

第二步:Replica容错

第三步:新Master重启故障机

第四步:数据恢复

#### Master选举的流程是什么?

触发Master选举首先有两个条件

如果你的Master节点宕机了就会调用findMaster方法,该方法会ping所有节点,看看当前集群有没有Master节点,如果没有就会进入Master选举,从候选节点(node.master:true)中选举Master,如果票数过半,就会选择出master节点,并停止选举

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

1.Green:所有p shard和r shard均为active,集群很健康

2.Yellow:至少一个replica shard不可以,但是数据仍然是完整的

3.Red:至少有一个p shard为不可用状态,数据不完整,集群不可用

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

- 4.更新数据

  - 4.1.全量替换
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

DELETE/index/_doc/id

