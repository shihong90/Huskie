#### Spark对应MapReduce的增强是什么?

一个数据集有两个逻辑一个是迭代逻辑比如:FlatMap将一条变多条,map一条还是一条.另一个是业务逻辑:这一条数据该如何处理.而MR需要我们自己实现数据迭代逻辑, Spark对MR的发展就是将数据集迭代逻辑都实现出各类算子,Spark程序只需要.相应算子传入其相关业务逻辑即可实现

#### 术语

Application:生命周期

job:项目

stage:阶段

task:任务

#### 比例

MR:1个app:1个job 他会返回销毁创建job

spark是1个app:Njob 他会复用资源

MR:1个job:1~2stage

Spark:1个job:N个stage

MR和spark没有区别:1stage:N个task

#### spark在一个app生命周期中RDD是可以复用的

将这个过程job1:stage1 -shuffle->stage2 与

job2:stage1 -shuffle->stage2-shuffle->stage3

通过复用变为:复用job1数据-shuffle->stage2  

job是阻塞的一个一个发生的,且上面有结果下面的是可以复用的

#### 一个stage阶段 以及这个阶段中的test描述的是什么?

描述的是在一台机器上可以顺滑完成的所有逻辑计算,也就是不出当前机器可以做的所有事情.stage与stage之间的边界就是shuffle,通过shuffle来将一台机器上的操作结果数据拉到另一台机器上继续进行操作

#### spark的编程模型是什么?

面向数据集的操作

#### RDD的分区数是如何确定的

```scala
val fileRdd: RDD[String] = sc.textFile(path="data/testdata.txt",16)
//RDD的分区数是取决于path,和minPartitions的最大值,如果文件有10个块,分区设置8个,RDD分区数就是10;如果文件块是8,分区设置为10,RDD分区数就是10
```

#### RDD的实现

RDD本身是抽象类,第一个子类实现就是:

HadoopRDD:解决的是文件数据的输入

MapPartitionRDD

ShuffleRDD

UnionRDD

#### InputFormat输入格式化类的作用是什么?

- 1.放切片，可以通过getSplits得到数据源有多少个切片基本上也就是有多少个分区
- 2.记录读取器RecordReader  对RecordReader记录读取器包装了一下得到compute方法,调用其得到文件的迭代器,通过调hashNext从数据源文件中得到一条一条的数据

#### 什么是切片,怎么算的,切片为啥等于分区数? 

一个核心的方法getPartitions下面的getsplits,传入两个参数:JobConf;你期望的分区数

通过JobConf来获取文件数组,通过一个for循环,面向每一个文件,获取文件的路径、大小、文件块大小、所有文件大小/期望分区数=goaSize期望切片大小,通过上述四个参数

进行计算逻辑如下:

```scala
Math.max(minsize,Math.min(goaSize,blockSize))
```

切片大小默认等于块大小

首先将文件大小赋给一个变量,在while循环中将其每次除以一个切片大小,看看其是否大于1.1,如果大于

```java
long bytesRemaining = length //文件大小赋给一个变量
    while(((double) bytesRemaining)/splitSize > SPLIT_SLOP){ //SPLIT_SLOP默认1.1
        String[][] spliitHosts = getSpliHostsAndCachedHosts(blkLocations,length-bytesRemaining,splitSize,clusterMap); //length-bytesRemaining 第一次8-8=0 第二次循环8-7=1 第三次循环8-6=2,这个其实就是他的offset偏移量
        splits.add(makeSplit(path,length-bytesRemaining,splitSize,splitHosts[0],spliyHosts[1]);
        bytesRemaining -=splitSize; //每次循环都去减一个切片大小
    }

```

#### 切片是由什么组成的?

```java
 splits.add(makeSplit(path,length-bytesRemaining,splitSize,splitHosts[0],spliyHosts[1]);
1.path:归属于那个文件 
2.length-bytesRemaining偏移量
3.splitSize:切片大小
4.splitHosts:切片主机地址 
```

#### 你new 出来一个RDD他里面存数据么?

RDD他是不存的,HadoopRDD里面实现是通过compute得到一个对文件的迭代器

#### flatMap源码剖析

通过SparkContext.textFile()得到了HadoopRDD,通过RDD.flatMap我们就得到了一个MappartitionRDD,他会传入三个参数:1.prev:指向前一个RDD,这就像是单向链表,通过这种机制就实现了lineAge血统

2.我们传入的函数变为了对象的成员属性

```java
 //这个cleanF就是我们传入flatMap中的参数
(context,pid,iter)=>iter.flatMap(cleanF)  //context上下文 pid分区id iter迭代器
```

3.deps与HadoopRDD是一比一关系,你有几个分区我就有几个分区

#### 我们创建的MappartitionRDD他的数据是怎么从HadoopRDD过来的?

MappartitionRDD他也会有compute,但是他和HadoopRDD的compute实现是不一样的,他是将传入的flatMap()中函数进行调启,会传入三个参数:1.context上下文 2.具体的分区 3.前一个RDD的.iterator的方法

当我调用mappartiton的iteator方法时,他就会调启当前RDD的compute,而compute方法他会调启前一个HadoopRDD的.iterator方法,该方法会调用当前RDD的compute方法,而当前compute方法就会通过Nextiterator拿到数据发送给MappartionRDD

rudeceBykey



#### combine在mapReduce中起到一个什么作用?

压缩减少io

#### combine是怎么实现的?

在map端有很多1111这个样的记录,你期望在map这一端把这一批数据,变成一条数据,这就是在map端做压缩,reduce拉取过来的数据会有一个合并的过程

#### 如果用一个HashMap实现将H1,H1,H1,H1=>H4这样的一个操作,他是怎样实现的?

1.第一条数据之间放入HashMap

2.第二条放入HashMap使用要做判断map中是否有这一条,有的话将这个两个val拿出来进行相加:新值+老值   

3.多次溢写整合:新值+老值    x+=y

#### reduceBykey的实现过程

redueceBykey底层是调用combineByKey里面要传入四个参数:1.(v:V)=>v  2 `_`+`_`   3. `_`+`_`   4.partioner分区器

在方法中最终会给你new出个ShuffledRDD,在ShuffledRDD参数定义中是对prev添加@transient注解的,因为ShuffledRDD是要靠shuffle拉取数据,这样就把这个RDD之间的依赖链割裂开来,前面的HadoopRDD->MappartitionRDD是一个stage1,后面的ShuffledRDD是stage2

如果不加@transient注解,等于先把前面的stage1的RDD进行序列化运行程序逻辑,但是当shuffledRDD后面要进行一个计算的话就会导致重复的进行stage1的逻辑.加@transient注解就会将stage1的逻辑结果变成中间结果文件,stage2通过shuffle获取进行后续的计算

shuffledRDD里面会重写RDD的getDependencies他里面有一个核心方法ShuffleDependency要传四个参数:1prev前置的RDD  2.part分区器  3.serializer序列化器  4.keyOrdering key是否排序

5.aggregator聚合区 6.mapSideCombine map端是否聚合

依然会有compute,里面是通过shuffleManager.getReader.read()就得到一个迭代器iterator,会迭代获取前面stage1生成的File.txt文件

#### @transient注解是什么意思?

忽略/打断序列化

#### ShuffleMapTask

里面会有一个runTask方法,他会准备一个ShuffleWriter写.他是通过shuffleManage.getWriter来获取到的,这个writer里面会有一个write方法

像write方法里面传入RDD的iterator会生成结果文件File.txt

#### 面向RDD的方法有哪几类?

- 1.create创建算子 :从上下文中获取RDD
  - textFile()
- 2.transformation转换算子:能通过RDD转化成新的RDD
  - 不需要产生shuffle
    - map,flatmap,filter
  - 产生shuffle
    - reduceBykey,groupByKey
    - 他们都是依赖shuffle,shuffle是需要分区器的,分区器一定是作用在键值对上的
  - active执行算子:能触发runJob()跑逻辑
    - foreach(),collect()回收算子,saveasfile()
  - controller控制算子
    - cache:让RDD缓存数据到内存里面,以便提高后续的计算效率
    - checkpoint:到外部的存储系统里面去

## spark底层底层计算形式是什么?

pipLine数据管道

一个node2节点上的Block01通过多个RDD之间的compute方法迭代调用一条条的数据最终形成File.txt文件,这样一个调用链路叫做pipLine数据管道

#### 为什么Fink火,批量计算的弊端是什么?

就是spark是批量计算他会有一个限制的PipeLine不跑完,前面的任务不能停,后面会被阻塞住,而fink属于纯流式计算,跟流水线一样,所有人都会动不会停着

#### sparkCore批计算为什么能取代MapReduce?/spark计算速度为什么比MR快?

1.spark申请资源时是粗粒度的资源申请:在任务调度的时候,每一个任务启动速度就变快了,并且task执行完毕之后,并不会把executor给kill掉,而是等所有的task执行完成之后才会释放资源,优势是:task启动速度变快,整体执行时间变短;弊是:浪费集群资源

2.spark基于内存来计算,将RDD的计算结果cache或者persist,包括pipline这种计算模式,每一个算子计算结果会立刻迁移到另一个算子

#### 一句话描述一下Hive

我们可以基于sql语句对海量数据做各种数据分析,也叫非结构化数仓

#### 描述一下Spark架构

在架构层是:

Master:作用是管理集群中所有的Worker,进而管理了集群资源

Worker:就是管理各个节点上的资源(内存,核数)

​	WORKER_MEMEORY 1G

​	WORKER_CORES 2

从task任务的角度来看是由:

Driver:负责任务的调度

Executor:他是真正做任务的执行

#### 在Spark中RDD有哪些依赖关系?

- 顶级父类Dependency

  - 子类实现ShuffleDependency 宽依赖

    一对多

  - 抽象类实现:NarrowDepency 窄依赖 (那若)

    - OneToOneDependency

      RDD的分区是一对一对对应关系

    - RangeDependency

      两个RDD得到一个RDD,多对一

#### 为什么要有一个Hive出现？

易用角度来看：他解决了MapRduece编程负责的缺点，改为SQL简单易于上手

在文件这个维度：他是有三块的:1.data数据文件，他是交给dataNode进行存储

2.元数据他描述的是文件级别的：文件的大小，创建时间，属主

3.数据的元数据类似于数据的二级索引：多少个列，列的类型，描述信息

从表的角度:生成了schema,和row.可以使用sql来便捷解决数据问题

table是虚构的映射是一个模型,表最终操作的还是文件







#### 迭代器模式



spark架构SparkContext源码实现：RPC、netty、NIO、序列化反序列化、堆外内存、零拷贝