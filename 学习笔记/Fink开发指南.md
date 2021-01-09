---
typora-root-url: ..\项目细节架构图
---

## Fink是什么?



#### 有界数据集Unbounded Data和无界数据集Bounded Data他们之间的区别是什么?

Unbounded streams有开始没有结束

#### 批处理Batch和流式处理Streaming有什么样的区别?



#### 一句话描述一下Hive

我们可以基于sql语句对海量数据做各种数据分析,也叫非结构化数仓

#### sparkCore批计算为什么能取代MapReduce?/spark计算速度为什么比MR快?

1.spark申请资源时是粗粒度的资源申请:在任务调度的时候,每一个任务启动速度就变快了,并且task执行完毕之后,并不会把executor给kill掉,而是等所有的task执行完成之后才会释放资源,优势是:task启动速度变快,整体执行时间变短;弊是:浪费集群资源

2.spark基于内存来计算,将RDD的计算结果cache或者persist,包括pipline这种计算模式,每一个算子计算结果会立刻迁移到另一个算子

