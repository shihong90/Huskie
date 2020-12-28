面向列的数据存储

HBase的高级特性如过滤器,协处理器,优化策略?



##### 为什么Hbase能做到很快?

1.顺序读取磁盘文件

2.充分利用了系统里面的内存

#####   什么叫NoSQL啊?

不仅仅是SQL

##### 什么叫面向列?

你在mysql上存储一条记录,一行你只有一列是存储数据的,其余都是Null,这些Null也同样占用空间,Mysql他要求的是结构化数据,定义表的scheam的时候已经将这些列定义好了,不管你有没有,都会占用空间.为了解决这种稀疏数据的问题而诞生了列式存储Hbase

不管你是行存,还是列存,最终还是要落地到文件中,也就是一行行的记录.

##### 什么叫非结构化数据?能举一些列子么?

json半结构化数据

##### Hbase数据模型

Row key 行键,所谓的行键类似于mysql的id主键一样

Time Stamp 时间戳,它起到的是控制版本的作用

CF1 列族,表示一组列的集合,列族是Hbase最小的控制单元

,你是不能控制列的,因为列族已经是最小控制单元了









zookeeper解决了hbase的单点故障，存储了hbase的很多元数据
hbase最终要实现的功能增删改查key-value

##### hbase是怎么去管理大量的key-value？

hbase的一行数据

行键rowkey cf1{				cf2{
	       id{时间戳1:数据，时间戳2：数据          age{ts1：18}
	       name{ts1：李四，ts2：王五	    department{ts1：AS}
	        }				}
hbase访问一个元素（rowkey行键，column family列族，key，value ==>map<timestamp,数据类型>)

hbase的访问 hbase.get(rowkey,cf,column,timestamp) = value   通过四个条件才能获得一个唯一的值

rowkey:就相当于mysql的主键id
cf:把一张表中的很多列字段进行分类管理
column:就相当于mysql的字段
timestamp:通过记录时间戳,来做版本管理,时间戳大的是新版本,小的是老版本
value:相当于mysql的字段的值

hbase中没有数据类型这个概念,统统都是字节数组

##### 访问Hbase表中的行数据有三种方式:

1.通过单个rowkey访问
2.通过row key的range,指定startKey和endKey
3.全表扫描


关于多版本管理:
1.最后的n个版本
	n=3 只会保存最新的3个值
2.最近的某几个版本
	n =某个时间戳 当大于n时的时间戳都要保存,其余小于的就认为过期不保存了

HMaster是主节点HRegionServer是从节点,每个从节点会去管理多个region,注意:一个regionServer管理的多个region不
一定是同一个表的,程序是通过zk来管理元数据信息实现定位那些region是一个表,那些是另一个表的

一个region里面又分为多个store,一个store就是一个列簇,一个store里面包含一个memStore,和多个HFile(128M)
多个Hfile合并生成数据我们叫StoreFile.

hbase的HMaster宕机了,不涉及元数据修改的比如查询都不受影响,总结:如果HMaster死掉了hbase集群能用,但是部分服务受限
涉及元数修改的:创建表、修改表、删除表不能做了,以及hbase的负载均衡,region的split分割和compact合并这些都不能做了

整个表会按照rowkey进行字典排序,
什么是region?
region就是一个hbase表的水平分段,当表内数据量达到10G是就水平切分一次,一个表变俩表,在继续向下存储数据

##### Hbase和Hive的比较

相同点:
	Hbase和Hive都是架构在Hadoop之上的,用HDFS做底层的数据存储,用MapReduce做数据计算
不同点:
	1.Hive是是为了降低MapReduce的编程复杂的ETL工具,而Hbase是为了弥补Hadoop对实时操作的缺陷
	2.Hive是逻辑表,Hbase是物理表
	3.Hive是数仓工具,需要全表扫描,hbase是数据库,进行的是索引访问
	4.Hive存入数据时不做校验,Hbase存入数据会合Mysql做scheam字段校验
	5.Hive不支持单行记录操作,Hbase支持单行记录的CRUD

学习hbase的shell操作的终极心法:
1.hbase shell 进入hbase的shell命令交互行
2.help  :观看所有名称,猜测用法
3.help "create" 查询命令的详细使用方式,以及使用案例

注意:
在hive、mysql中:数据库叫database
而hbase中数据库不叫database改叫:namespace

qualifier:列族
Cell(rowkey,quualifier,column,timesamp,value)

这一个{ }就是一个列族的详细信息
{name => 'cf1', bloomfilter =>'row',versions=>'1',in_memory=>'false',keep_deleted_cells=>'false',data_block_encoding='none'
,ttl=>'forever',compression=>'none',min_versions=>'0',blockcache=>'true'}

name => 'cf1':列族的名字    
bloomfilter =>'row':当前列族要不要启用布隆过滤器  
versions=>'1':存储几个版本 
in_memory=>'false':如果是true是将这个store全部放在内存里面
ttl=>'forever':永久有效
keep_deleted_cells=>'false':
compression=>'none'压缩
blockcache=>'true':启不启用读缓存

##### 为什么要进行hbase表的预分区?

你刚开始创建表的时候,这个表必定是不足10G的,所以肯定这张表只有一个region
其实,也可以让一张表刚创建的时候,就初始化成100个region,分担大批量数据写入压力




mybatis的API操作3步骤:
1.先构建会话工厂
	SqlSessionFactory工厂类
2.通过工厂类创建一个实例
	SqlSession会话
3.通过会话做各种操作
	sqlSession.selectOne();


JDBC的API操作5个步骤:
1.注册驱动类
Class.forname("com.jdbc.Driver")
2.通过DriverManager获取连接对象(会话)
	Connection con = DriverManager.getConnection(url,password)
3.通过con获取一个statement对象
	Statement stat = con.preapareStatement();
4.通过stat执行SQL
	stat.execQuery("select * from student")
5.解析结果ResultSet


HDFS的API操作:
1.创建一个Configuration来管理一下配置信息
	Configuration conf = new Configuration()
	加载配置有以下几种方式:	
		conf.addResource()
		conf.set(key,value)
		conf.loadFile(confFile)
2.通过这个对象创建一个FileSystem
	FileSystem fs = FileSystem.get(conf)
3.调用fs的各种实例操作HDFS
	fs.copyFromLocakFile()    //上传
	fs.copyToloaclFile()          //下载
4.关闭会话:
	fs.close()


SparkSQL的API操作:
1.创建一个SparkSession入口
	val spark = SparkSession.builder().appMaster("").config().getorCreate()
2.通过sparkSession来读取数据转换为DataFrame数据类型
	val df = spark.read.format("").
3.通过对DF创建临时表,进行sql操作
	val tmpDF = df.createTable("tmp_table")
	tmpDF.sql("select * from tmp_table")
4.关闭会话
	spark.stop()


SparkCore的API操作
1.创建一个SparkSession入口:
	val spark = SparkSession.builder().appMaster("").config().getorCreate()
2.通过sparkSession对象创建一个sparkContext上下文
	val sc = spark.SparkContext()
3.通过sc对象读取数据转化为RDD数据类型:
	val Rdd = sc.
4.通过Rdd对象的算子实现对业务数据的操作
	Rdd.
5.关闭会话:
	sc.stop()


Zookeeper的API操作,有2个类:
1.获取Zookeeper会话连接:

2.Watcher

Kafka的API操作

Hbase的JavaAPI操作是比较复杂有15个类:
HbaseAdmin:创建表,删除表,判断表是否存在,修改表....管理HBase当中所有的表的元数据
	hbase.createTbale(HtableDescrptor)
	hbase.dropTable(HtableDescrptor)
	hbase.alterTable(HtableDescrptor)
	hbase.tableExixts(HtableDescrptor)

	HtableDescrptor的内部,可以变成由两部分组成:
		1.表名:TbaleName
		2.列族:HColumnDescriptor

HTable:对一张表进行抽象的,提供四个方法用来做增删改查
	Result result = htable.get(Get get) 	查询数据 1.多个get重载方法,2.更通用的参数
	int i = htable.put(Put put) 	插入数据 2.
	int i = htable.delete(Delete delete)  删除数据
	ResultScanner rs = htable.scan(Scan scan) 扫描数据
Row:
	Mutation表示涉及到修改的	Delete Put
	Get/Scan查询的		


Result内部就是由多个Cell/keyVlaue组成的
ResultScanner的内部就是由多个Result组成

HbaseConnection:连接对象,通过连接对象就可以获取到HTable这个实例
HbaseConfiguration:管理所有的配置


使用示例使用Hbase的API实现数据插入:
//1.获取配置conf对象
HbaseConfiguration conf = new HbaseConfiguration();

//2.通过传入conf配置对象来创建连接
HbaseConnection con = HbaseConnection.create(conf);

//3.通过获取到抽象表
HTable table = con.getHTable("nx004");

//4.1构造Put对象
Put put = new Put(rowkey);
put.addColumn(cf,column,ts,value); //指定列簇,列,时间戳,值
put.addColumn(cf,column,ts,value); //添加多个key,value
put.addColumn(cf,column,ts,value); //添加多个key,value
put.addColumn(cf,column,ts,value); //添加多个key,value

//4.2通过抽象表插入数据
table.put(put)



##### 创建对象有几种方式?

1.调用构造方法new XXX()
2.静态工厂方法xxx.newInstance()、XXX.get()
3.反射
4.克隆
5.反序列化

企业级:
1.过滤器
2.分页
3.整合Hive和MapReduce

过滤器查询涉及三个规则
1.比较规则-->大于,小于,等于,不等于....
	=
	<>不等于
2.比较器-->指定比较机制
	SubstringComparator("abc")  		子串
	RegexStringComparator([a-c]{1-5})    	正则
3.过滤器
	ValueFilter针对cell中的value属性做过滤 
根据一个Cell对象中的value属性,做substring规则的过滤,=当前这个字符串的就保留==>value中包含abc子字符串的字段就过滤出来了
过滤器hbase的shell操作:
get 'user_info','user0000',{filter =>"(QualifierFilter(=,'substring:s'))"}

分页过滤器
pageNumber 每页多少条
pageIndex第几页
pageNumber * (pageIndex-1)











