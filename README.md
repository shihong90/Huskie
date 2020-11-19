# Huskie大数据平台开发手册

# 产品矩阵

![输入图片说明](https://images.gitee.com/uploads/images/2020/1103/205450_f36ea591_7955977.jpeg "全局项目架构.jpg")
# 1.项目简介

## 1.1AP数据采集平台

通过编写和配置数据提取脚本,实现将数据库数据和日志埋点数据统一拉取到Hadoop集群上

![输入图片说明](https://images.gitee.com/uploads/images/2020/1103/205543_5243ec32_7955977.png "AP数据采集平台架构图.png")



## 1.2DW离线数据仓库

是为集团所有级别的决策制定过程，提供所有类型数据支持的战略集合。



## 1.3RTDW实时数据仓库

市面上对实时数仓没有范式的定义,像一线大厂,尤其是阿里,付费购买的实时服务,全部SQL化开发,功能异常强大,当然我们这边是做不到完全SQL化的,还是需要使用代码来实现部分的功能

## 1.4DMP用户画像系统

为集团实现精准营销提供技术支持

## 1.5MSDQAP多源数据查询平台

由于集团早期没有统一规划数据源，导致全局混乱,数据杂乱存储在:多台台机器、第三方、公司内部多个系统,并且还存储在多种不同引擎如：redis、sql Server、hbase、hdfs、elasticsearch、kafka上，所以不好规划统一 ，导致查询关联及其麻烦,要各种预先抽取多个数据源到同一个地方，然后在做统一处理，最后出报表 ，而且查询及其缓慢.为了解决此问题,我针对性的设计了MSDQAP多源数据查询平台.

[逻辑架构]

![1605501033902](C:\Users\monster\AppData\Local\Temp\1605501033902.png)

[项目架构]

![1605500964377](C:\Users\monster\AppData\Local\Temp\1605500964377.png)



## 1.6Naga数据分析平台

给集团数据挖掘、数据分析师提供一个:查询引擎可自定选择(MR、Spark)、集群高可用(随便折腾)、查询数据有权限控制,资源使用有控制、查询SQL语句模板化、傻瓜式操作、拖拽式操作这种查询OA系统,实现简单操作快速检索查询,实时生成数据分析结果报表,实现查询即产出

## 1.7用户兴趣取向性分析

## 1.8.ELK日志检索平台



## 1.8延伸项目

### 1.8.1Hadoop源码二次开发

1)Hadoop 集群在高并发情况下不工作现象

2)在大数据集情况下 NameNode 会因为 fullGC 直接退出

3)Hadoop 锁的性能不高

4)NameNode 写数据流程性能不高
我们要实现对Hadoop进行二次开发,通过修改 Hadoop 源码实现对 NameNode 的 Bug 修复以及对 NameNode 的优化,使 HDFS 集群稳定性达到 9999,一年 365 天稳定运行时间 99.99%

### 1.8.2**海量数据的接收和落地**

初期业务的订单是以Mysql、sql Server作为业务库，但是随着业务线增多，每日新增数据指数上涨，几乎在每天的高峰期期间，都会出现业务库所在服务器的CPU、IO、内存等跑满。因此，需要将每日的azkaban定时采集数据任务，从业务库迁移出来到Hbase上,即要实现通过mysql的binlog日志使业务库和Hbase的采集库数据实现实时同步

### 1.8.3电商微信小程序

### 1.8.4Spark的离线以及流式数据处理的细粒度监控

在Spark的4040页面所提供的监控面板上监控指标并不多,为了满足后期提交job以及定位Spark离线计算以及流计算过程中所出现的Bug我们通过开发SparkListen和StreamListen这两个回调接口,实现Spark执行的细粒度监控大屏

### 



# 2.前期准备工作

## 2.1版本选型

Apache √

CHD 

依据:考虑到2020年CDH开始变为论节点付费模式,一个节点1万美金,大部分公司选择不升级CDH或者转为Apache系统,Apache系统开源免费,最大问题就是不易维护和版本不兼容性问题,尤为突出

## 2.2服务器选型

物理机

阿里云主机(标准版:128G 8T) √

依据:考虑到前期业务量比较小,业务规模呈现线性增长,考虑到阿里云简单,便宜,易扩展,易于维护,所以我们选择阿里云进行大数据集群的搭建

## 2.3集群资源规划设计

假设:每台服务器8T磁盘,128G内存

(1)每天日或跃用户100万,每人一天平均100条:100万*100条=1亿条

(2)每条日志1K作用,每条一亿条:100000000/1024/1024=约100G

(3)半年内不扩容服务器来算:100G*180天=约18T

(4)保存3副本:18T*3=54T

(5)预留20%-30%余量=54T/0.7=77T

(6)约8T*10台服务器

## 2.4全局集群服务规划架构

|           | 服务器1          | 服务器2                    | 服务器3                   |      |      |
| --------- | ---------------- | -------------------------- | ------------------------- | ---- | ---- |
| HDFS      | NameNodeDataNode | DataNode                   | DataNodeSecondaryNameNode |      |      |
| Yarn      | NodeManager      | ResourcemanagerNodeManager | NodeManager               |      |      |
| Zookeeper | Zookeeper        | Zookeeper                  | Zookeeper                 |      |      |
| Kafka     | Kafka            | Kafka                      | Kafka                     |      |      |
| Flume     | Flume负责采集    | Flume负责采集              | Flume负责消费             |      |      |
| Hive      |                  |                            |                           |      |      |
| Mysql     |                  |                            |                           |      |      |
| Presto    |                  |                            |                           |      |      |
| Kylin     |                  |                            |                           |      |      |



# 3.AP数据采集平台

## 3.1项目需求分析

日志采集系统-->文件

业务系统-->mysql数据库   --->AP数据采集平台-->把各路数据汇总到DW数仓

爬虫-->mongodb数据库



## 3.2技术选型

日志采集:Flume

业务库数据迁移:Sqoop、DataX

中间件:Kafka

## 3.3AP数据平台架构

![输入图片说明](https://images.gitee.com/uploads/images/2020/1103/205543_5243ec32_7955977.png "AP数据采集平台架构图.png")
### 3.4数据生成模块

#### 3.4.1埋点数据基本格式

公共字段：基本所有安卓手机都包含的字段

```
{
"ap":"xxxxx",//项目数据来源 app pc
"cm": {  //公共字段
		"mid": "",  // (String) 设备唯一标识
        "uid": "",  // (String) 用户标识
        "vc": "1",  // (String) versionCode，程序版本号
        "vn": "1.0",  // (String) versionName，程序版本名
        "l": "zh",  // (String) language系统语言
        "sr": "",  // (String) 渠道号，应用从哪个渠道来的。
        "os": "7.1.1",  // (String) Android系统版本
        "ar": "CN",  // (String) area区域
        "md": "BBB100-1",  // (String) model手机型号
        "ba": "blackberry",  // (String) brand手机品牌
        "sv": "V2.2.1",  // (String) sdkVersion
        "g": "",  // (String) gmail
        "hw": "1620x1080",  // (String) heightXwidth，屏幕宽高
        "t": "1506047606608",  // (String) 客户端日志产生时的时间
        "nw": "WIFI",  // (String) 网络模式
        "ln": 0,  // (double) lng经度
        "la": 0  // (double) lat 纬度
    },
"et":  [  //事件
            {
                "ett": "1506047605364",  //客户端事件产生时间
                "en": "display",  //事件名称
                "kv": {  //事件结果，以key-value形式自行定义
                    "goodsid": "236",
                    "action": "1",
                    "extend1": "1",
"place": "2",
"category": "75"
                }
            }
        ]
}
示例日志（服务器时间戳 | 日志）：
1540934156385|{
    "ap": "gmall", 
    "cm": {
        "uid": "1234", 
        "vc": "2", 
        "vn": "1.0", 
        "la": "EN", 
        "sr": "", 
        "os": "7.1.1", 
        "ar": "CN", 
        "md": "BBB100-1", 
        "ba": "blackberry", 
        "sv": "V2.2.1", 
        "g": "abc@gmail.com", 
        "hw": "1620x1080", 
        "t": "1506047606608", 
        "nw": "WIFI", 
        "ln": 0
    }, 
        "et": [
            {
                "ett": "1506047605364",  //客户端事件产生时间
                "en": "display",  //事件名称
                "kv": {  //事件结果，以key-value形式自行定义
                    "goodsid": "236",
                    "action": "1",
                    "extend1": "1",
"place": "2",
"category": "75"
                } 
            },{
		        "ett": "1552352626835",
		        "en": "active_background",
		        "kv": {
			         "active_source": "1"
		        }
	        }
        ]
    }
}
```

业务字段：埋点上报的字段，有具体的业务类型

下面就是一个示例，表示业务字段的上传。

#### 3.4.2事件日志数据

.............

### 3.5埋点数据的生成

我们考虑到目前公司还没有埋点用户行为数据,但是对未来的业务开发不能中断,所以我们通过代码来模拟生成数据

#### 3.5.1生产埋点数据Demo

我们通过创建一系列的Bean对象通过fastjson在将Bean对象转换为Json对象

以下是fastjson的使用示例:

```
JSON.toJSONString适用场景:单层{ }json串
//通过传入实体类得到一个:字符串json
String jsonString = JSON.toJSONString(传入实体类) 
```

```
JSONObject适用场景:是嵌套{ }的json串,一个JSONObject就是一个大括号{ }
JSONObject json = new JSONObject();

//如何将实体类转换成JSONObject-->传入JSONObject中
JSONObject jsonObject = (JSONOnject)JSON.toJSON(实体类) 

json.put("ap","app")
json.put("cm",JSONObject对象)
json.put("et",JSONArray对象)
json.put("kv",JSONObject对象)
```

```
JSONArray适用场景是:当json串中嵌套了[ ]时
 JSONArray eventsArray = new JSONArray();
 怎么向json数组中添加数据
 eventsArray.add(JSONObject对象)
```

#### 3.5.2数据生成脚本

1）在/home/atguigu/bin目录下创建脚本lg.sh

```
[atguigu@hadoop102 bin]$ vim lg.sh
```

2）在脚本中编写如下内容

104用来我们flume消费kafka数据上传到hdfs这块

```
#! /bin/bash

for i in hadoop102 hadoop103 
do
	ssh $i "java -jar /opt/module/log-collector-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 >/dev/null 2>&1 &"
done
```

3）修改脚本执行权限

```
[atguigu@hadoop102 bin]$ chmod 777 lg.sh
```

4）启动脚本

```
[atguigu@hadoop102 module]$ lg.sh 
```

5）分别在hadoop102、hadoop103的/tmp/logs目录上查看生成的数据

```
[atguigu@hadoop102 logs]$ ls

app-2020-03-10.log

[atguigu@hadoop103 logs]$ ls

app-2020-03-10.log
```

### 3.6Hadoop运行环境搭建

### 3.6.1编写集群分发脚本xsync

（3）案例实操

（a）在hadoop102上，将hadoop102中/opt/module目录下的软件拷贝到hadoop103上。

```
[atguigu@hadoop102 ~]$ scp -r /opt/module/*  atguigu@hadoop103:/opt/module
```

（b）在hadoop104上，将hadoop102服务器上的/opt/module目录下的软件拷贝到hadoop104上。

```
[atguigu@hadoop104 opt]$ scp -r atguigu@hadoop102:/opt/module/* atguigu@hadoop104:/opt/module
```

注意：拷贝过来的/opt/module目录，别忘了在hadoop102、hadoop103、hadoop104上修改所有文件的，所有者和所有者组。sudo chown atguigu:atguigu -R /opt/module

**2）rsync远程**同步工具

rsync主要用于备份和镜像。具有速度快、避免复制相同内容和支持符号链接的优点。

rsync和scp区别：用rsync做文件的复制要比scp的速度快，rsync只对差异文件做更新。scp是把所有文件都复制过去。

（1）基本语法

```
rsync    -av       $pdir/$fname              $user@hadoop$host:$pdir/$fname
```

命令   选项参数   要拷贝的文件路径/名称    目的用户@主机:目的路径/名称

选项参数说明

| 选项 | 功能         |
| ---- | ------------ |
| -a   | 归档拷贝     |
| -v   | 显示复制过程 |

（2）案例实操

（a）将hadoop102中/etc/profile.d/my_env.sh文件拷贝到hadoop103的/etc/profile.d/my_env.sh上。

```
[atguigu@hadoop102 ~]$ sudo rsync -av /etc/profile.d/my_env.sh root@hadoop103:/etc/profile.d/my_env.sh
```

（b）将hadoop102中/etc/profile文件拷贝到hadoop104的/etc/profile上。

```
[atguigu@hadoop102 ~]$ sudo rsync -av /etc/profile.d/my_env.sh root@hadoop104:/etc/profile.d/my_env.sh
```

注意：拷贝过来的配置文件别忘了source一下/etc/profile，。

3）xsync集群分发脚本

（1）需求：循环复制文件到所有节点的相同目录下

（2）需求分析：

​	（a）rsync命令原始拷贝：

```
rsync  -av     /opt/module  		 root@hadoop103:/opt/
```

​	（b）期望脚本：

xsync要同步的文件名称

​	（c）说明：在/home/atguigu/bin这个目录下存放的脚本，atguigu用户可以在系统任何地方直接执行。

（3）脚本实现

​	（a）在用的家目录/home/atguigu下创建bin文件夹

```
[atguigu@hadoop102 ~]$ mkdir bin
```

​	（b）在/home/atguigu/bin目录下创建xsync文件，以便全局调用

```
[atguigu@hadoop102 ~]$ cd /home/atguigu/bin

[atguigu@hadoop102 ~]$ vim xsync
```

在该文件中编写如下代码

```
#!/bin/bash
#1. 判断参数个数
if [ $# -lt 1 ]
then
  echo Not Enough Arguement!
  exit;
fi
#2. 遍历集群所有机器
for host in hadoop102 hadoop103 hadoop104
do
  echo ====================  $host  ====================
  #3. 遍历所有目录，挨个发送
  for file in $@
  do
    #4 判断文件是否存在
    if [ -e $file ]
    then
      #5. 获取父目录
      pdir=$(cd -P $(dirname $file); pwd)
      #6. 获取当前文件的名称
      fname=$(basename $file)
      ssh $host "mkdir -p $pdir"
      rsync -av $pdir/$fname $host:$pdir
    else
      echo $file does not exists!
    fi
  done
done
```

​	（c）修改脚本 xsync 具有执行权限

```
[atguigu@hadoop102 bin]$ chmod +x xsync
```

​	（d）测试脚本

```
[atguigu@hadoop102 bin]$ xsync xsync
```

#### 3.6.2Hadoop时间同步脚本（非正规临时脚本,测试使用）

1）在/home/atguigu/bin目录下创建脚本dt.sh

```
[atguigu@hadoop102 bin]$ vim dt.sh
```

2）在脚本中编写如下内容

```
#!/bin/bash

for i in hadoop102 hadoop103 hadoop104
do
    echo "========== $i =========="
    ssh -t $i "sudo date -s $1"
done
```

注意：ssh -t 通常用于ssh远程执行sudo命令

3）修改脚本执行权限

```
[atguigu@hadoop102 bin]$ chmod 777 dt.sh
```

4）启动脚本

```
[atguigu@hadoop102 bin]$ dt.sh 2020-03-10
```

#### 3.6.3集群所有进程查看脚本

1）在/home/atguigu/bin目录下创建脚本xcall.sh

```
[atguigu@hadoop102 bin]$ vim xcall.sh
```

2）在脚本中编写如下内容

```
#! /bin/bash

for i in hadoop102 hadoop103 hadoop104
do
    echo --------- $i ----------
    ssh $i "$*"
done
```

3）修改脚本执行权限

```
[atguigu@hadoop102 bin]$ chmod 777 xcall.sh
```

4）启动脚本

```
[atguigu@hadoop102 bin]$ xcall.sh jps
```

#### 3.6.2HDFS存储多目录

若HDFS存储空间紧张，需要对DataNode进行磁盘扩展。

1）在DataNode节点增加磁盘并进行挂载。

![img](file:///C:\Users\monster\AppData\Local\Temp\ksohtml5184\wps1.jpg) 

2）在hdfs-site.xml文件中配置多目录，注意新挂载磁盘的访问权限问题。

```
<property>
    <name>dfs.datanode.data.dir</name>
<value>file:///${hadoop.tmp.dir}/dfs/data1,file:///hd2/dfs/data2,file:///hd3/dfs/data3,file:///hd4/dfs/data4</value>
</property>
```

3）增加磁盘后，保证每个目录数据均衡

开启数据均衡命令：

```
bin/start-balancer.sh –threshold 10
```

对于参数10，代表的是集群中各个节点的磁盘空间利用率相差不超过10%，可根据实际情况进行调整。

停止数据均衡命令：

```
bin/stop-balancer.sh
```

#### 3.6.3LZO创建索引

#### 3.6.4对集群进行基准测试(压测)

1)测试HDFS写性能

测试内容：使用Hadoop自带demo向HDFS集群写10个128M的文件

```
[atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar TestDFSIO -write -nrFiles 10 -fileSize 128MB



2020-04-16 13:41:24,724 INFO fs.TestDFSIO: ----- TestDFSIO ----- : write
2020-04-16 13:41:24,724 INFO fs.TestDFSIO:             Date & time: Thu Apr 16 13:41:24 CST 2020
2020-04-16 13:41:24,724 INFO fs.TestDFSIO:         Number of files: 10
2020-04-16 13:41:24,725 INFO fs.TestDFSIO:  Total MBytes processed: 1280
2020-04-16 13:41:24,725 INFO fs.TestDFSIO:   Throughput mb/sec: 8.88 //吞吐量M/s
2020-04-16 13:41:24,725 INFO fs.TestDFSIO:  Average IO rate mb/sec: 8.96
2020-04-16 13:41:24,725 INFO fs.TestDFSIO:   IO rate std deviation: 0.87
2020-04-16 13:41:24,725 INFO fs.TestDFSIO:      Test exec time sec: 67.61
```

2)测试HDFS读性能

测试内容：使用Hadoop自带demo读取HDFS集群10个128M的文件

```
atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar TestDFSIO -read -nrFiles 10 -fileSize 128MB



2020-04-16 13:43:38,857 INFO fs.TestDFSIO: ----- TestDFSIO ----- : read
2020-04-16 13:43:38,858 INFO fs.TestDFSIO:   Date & time: Thu Apr 16 13:43:38 CST 2020
2020-04-16 13:43:38,859 INFO fs.TestDFSIO:         Number of files: 10
2020-04-16 13:43:38,859 INFO fs.TestDFSIO:  Total MBytes processed: 1280
2020-04-16 13:43:38,859 INFO fs.TestDFSIO:       Throughput mb/sec: 85.54
2020-04-16 13:43:38,860 INFO fs.TestDFSIO:  Average IO rate mb/sec: 100.21
2020-04-16 13:43:38,860 INFO fs.TestDFSIO:   IO rate std deviation: 44.37
2020-04-16 13:43:38,860 INFO fs.TestDFSIO:      Test exec time sec: 53.61
```

3）删除测试生成数据

```
[atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar TestDFSIO -clean
```

4）使用Sort程序评测MapReduce

​	a.使用RandomWriter来产生随机数，每个节点运行10个Map任务，每个Map产生大约1G大小的二进制随机数

```
[atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.3.jar randomwriter random-data
```

​	b.执行Sort程序

```
[atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.3.jar sort random-data sorted-data
```

​	c.验证数据是否真正排好序了

```
[atguigu@hadoop102 mapreduce]$ hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar testmapredsort -sortInput 
```



### 3.7Zookeeper集群的搭建

### 3.7.1Zookeeper的安装

### 3.7.2Zookeeper集群启动停止脚本

1）在hadoop102的/home/atguigu/bin目录下创建脚本

```
[atguigu@hadoop102 bin]$ vim zk.sh
```

​	在脚本中编写如下内容

```
#!/bin/bash
# 启动
case $1 in # 如果$1配置上了就执行start命令
"start"){
    for i in hadoop102 hadoop103 hadoop104  # 多台服务器循环遍历,执行下面这个命令
    do  # 这个do...done是相当于java的大括号{ }
        echo "------------- $i -------------" 
        # 先通过ssh $i 通过shh登录到i这个服务器执行下面的命令
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh start"
    done 
};;
# 停止
"stop"){
    for i in hadoop102 hadoop103 hadoop104
    do
        echo "------------- $i -------------"
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh stop"
    done
};;
# 查看状态
"status"){
    for i in hadoop102 hadoop103 hadoop104
    do
        echo "------------- $i -------------"
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh status"
    done
};;
esac
```

2）增加脚本执行权限

```
[atguigu@hadoop102 bin]$ chmod 777 zk.sh
```

3）Zookeeper集群启动脚本

```
[atguigu@hadoop102 module]$ zk.sh start
```

4）Zookeeper集群停止脚本

```
[atguigu@hadoop102 module]$ zk.sh stop
```

### 3.7 采集日志Flume

#### 3.7.1Flume的安装



#### 3.7.2Flume组件选择

1)Source(**batchSize在Event 1K左右时,500-1000合适**)

TailDir Source:断点续传、多目录

2)Channel

采用Kafka Channel,省去了Sink提高了效率.KafkaChannal数据存储在Kafka里面.

所以数据是存储在磁盘中

#### 3.7.3Flume的配置选择

![未命名文件](C:\Users\monster\Desktop\未命名文件.png)

（1）在/opt/module/flume/conf目录下创建file-flume-kafka.conf文件

[atguigu@hadoop102 conf]$ vim file-flume-kafka.conf

在文件配置如下内容

```
a1.sources=r1
a1.channels=c1 c2

# configure source
a1.sources.r1.type = TAILDIR
a1.sources.r1.positionFile = /opt/module/flume/test/log_position.json # 存放的是断点续传的索引位置
a1.sources.r1.filegroups = f1
a1.sources.r1.filegroups.f1 = /tmp/logs/app.+ # 读取数据的目录
a1.sources.r1.fileHeader = true
a1.sources.r1.channels = c1 c2  # 发往c1,c2

#interceptor拦截器
a1.sources.r1.interceptors =  i1 i2
# ETL拦截器
a1.sources.r1.interceptors.i1.type = com.custom.flume.interceptor.LogETLInterceptor$Builder 
a1.sources.r1.interceptors.i2.type = com.custom.flume.interceptor.LogTypeInterceptor$Builder

# channal选择器
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = topic # 靠这个头来确定数据写到那个里面 
a1.sources.r1.selector.mapping.topic_start = c1  # 就是一个kv对(topic,topic_start),如果去除的v值是topic_start我们就发往c1,如果是v的值是topic_event就发往c2
a1.sources.r1.selector.mapping.topic_event = c2

# 配置kafkaChannel
a1.channels.c1.type = org.apache.flume.channel.kafka.KafkaChannel
a1.channels.c1.kafka.bootstrap.servers = hadoop102:9092,hadoop103:9092,hadoop104:9092
a1.channels.c1.kafka.topic = topic_start  # kafka的topic
a1.channels.c1.parseAsFlumeEvent = false # 这里如果是true就会在数据前增加一个topic头,这就产生一个脏数据
a1.channels.c1.kafka.consumer.group.id = flume-consumer # 定义一个消费者组

a1.channels.c2.type = org.apache.flume.channel.kafka.KafkaChannel
a1.channels.c2.kafka.bootstrap.servers = hadoop102:9092,hadoop103:9092,hadoop104:9092
a1.channels.c2.kafka.topic = topic_event
a1.channels.c2.parseAsFlumeEvent = false
a1.channels.c2.kafka.consumer.group.id = flume-consumer
```

#### 3.7.4Flume的ETL和分类型拦截器

本项目中自定义了两个拦截器，分别是：ETL拦截器、日志类型区分拦截器。

ETL拦截器主要用于，过滤时间戳不合法和Json数据不完整的日志

日志类型区分拦截器主要用于，将启动日志和事件日志区分开来，方便发往Kafka的不同Topic。

### 3.8Kafka安装

#### 3.8.1



#### 3.8.2



#### 3.8.9Kafka的压测

Kafka官方自带压力测试脚本（kafka-consumer-perf-test.sh、kafka-producer-perf-test.sh）。Kafka压测时，可以查看到哪个地方出现了瓶颈（CPU，内存，网络IO）。一般都是网络IO达到瓶颈。

#### 3.8.10Kafak的集群数据规划

Kafka机器数量（经验公式）=2*（峰值生产速度*副本数/100）+1

先拿到峰值生产速度，再根据设定的副本数，就能预估出需要部署Kafka的数量。

假设我们的峰值生产速度是50M/s。副本数为2。

Kafka机器数量=2*（50*2/100）+ 1=3台

#### 3.8.11Flume消费Kafka数据测试数据管道是否打通

### 

# 4.DW离线数据仓库建设

## 4.1项目需求

1.数仓维度建模

2.分析:用户、流量、会员、销售、地区、活动统计指标

3.采用即席查询工具,随时进行指标分析

4.对集群性能进行监控,发送异常需要报警

5.元数据管理

6.质量监控

## 4.2技术选型



## 4.3DW平台架构

## 4.4

# 5.RTDW实时数据仓库建设

## 5.2项目需求分析



## 5.3技术选型



## 5.4DMP平台架构

# 6.DMP用户画像系统

## 6.2项目需求分析



## 6.3技术选型



## 6.4DMP平台架构

# 7.MSDQAP多源数据查询平台

## 7.2项目需求分析



## 7.3技术选型



## 7.4DMP平台架构

# 8.Naga数据分析平台

## 8.2项目需求分析



## 8.3技术选型



## 8.4DMP平台架构

## 

## 