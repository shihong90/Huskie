---
typora-root-url: ..\项目细节架构图
---

#### 什么是Flink?

就是一个分布式的有状态的计算引擎,主要是能够计算无界的数据流,和有界的数据流

#### 批处理Batch和流式处理Streaming有什么样的区别?

#### 什么叫做有状态的计算?

有无状态的核心就是是否关联了上下文context,比如hello这个单词统计了3次,如果在来一个hello,有状态的就会关联上下文,将hello变为4次,无状态就会认为只是1次

有状态的计算:每次仅限数据计算的时候基于之前数据的计算结果做计算,并且每次计算结果都会保存到存储介质中,计算关联上下文contxtx,基于有状态的计算不需要将历史数据重新计算,提供了效率

无状态的计算:每次进行数据计算只是考虑当前数据,不会使用之前数据的计算结果

在sparkStreaming实现有状态计算是使用updateStateByKey+checkpoint借助第三方存储介质如:redis实现有状态计算,而Fink就可以轻松实现,有状态计算属于Fink的一大特色

#### Fink的优势是什么？

1.同时支持高吞吐、低延迟、高性能

2.支持事件实际Event Time概念,结合Watemark处理乱序数据

3.支持有状态计算,并且支持多种状态 内存、文件、RocksDB

4.支持高度灵活的窗口Window操作 time、count、session

5.基于轻量级分布式快照CheckPoint实现容错保证exactly-once语义

6.基于JVM实现独立的内存管理

7.Save Points保存点 方便代码的升级

#### 什么是有界流什么是无界流?

无界流:定义了开始但是没有定义结束,比如说:用户日志,系统上线的那一天就是数据产生的开始,他是源源不断产生的

有界流:定义了开始定义了结束,有始有终,就是批计算

在Flink里面认为批计算是流计算的一个特例而已,但是早SparkStreaming的世界观里面他认为流计算是批计算的一个特例而已,所以SparkStreaming的理念就是把批变小,变成微批,这就是流计算

#### 描述一下Fink架构

Fink也是master-slave架构

JobManager(JVM进程) 主节点:资源的调度,任务的调度

TaskManager(JVM基础) 从节点

#### task slot是什么?

TaskManager中资源是使用task slot进行内存层面隔离,CPU(核)是共享的

比如Task Manager 3G 3core,3个task slot每个就是1G,所谓的task slot槽就是一组固定的资源,他类似于spark的Executor

task slot个数与core的个数是一一对应的

如果你的core是超线程,task slort = 2*cores

#### checkPoint是什么?

当下所有节点会对当下结果状态进行保存,就类比虚拟机的快照 

#### Task是如何进行分发的?

#### Fink on yarn的好处是什么?

1.分担jobManager的压力,jobManager只需要做任务管理,而资源管理就交个yarn了

2.降低维护成本

#### 我Fink任务往yarn上提交需不需开启HDFS?

必须要,一个client客户端要向yarn提交任务,首先要将jar包和其配置文件,上传到HDFS上,默认的存储目录是:/tmp/hadoop-yarn

第二步才会去ResoureManager申请资源,这个时候ResoureManager会看旗下的NodeManager那个资源会比较充足,这时他就会分配一部分资源,同时去HDFS下:/tmp/hadoop-yarn目录上去下载jar包依赖,然后在这个节点上去启动JobManager任务

第三步:启动后JobManager会为TaskManager向ResoureManager申请资源

第四步:ResoureManager会去看那个NodeManager只有资源,比如:会将NodeManager2资源分配给他,并启动TaskManager进程

第五步:JobManager会分发task到TaskManager中运行

#### Fink任务提交到yarn都有哪些模式?

1.yarn-session模式,在提交之前,需要先去yarn中启动fink集群给他起名yarn-session,启动成功后通过flink run 这个命令去往yarn-seesion中提交任务,当job执行完毕,yarn-session集群,并不会关闭,等待下个job的提交,他会一直占据集群资源,

每个job启动时间变短了

2.单个提交,直接在yarn运行一个flink任务,在jon执行之前,先去启动一个flink集群,集群启动成功job在执行,当job执行完毕,flink集群一同会被关闭,释放资源,虽然节省资源,但是每一个job启动时间边长

#### HA模式都有哪些

- 主备HA模式:当主进程挂了之后,备进程直接接管,进行工作
  - spark
  - yarn
- 重启HA模式:另外找一天节点重启
  - flink