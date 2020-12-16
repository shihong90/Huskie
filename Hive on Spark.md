---
typora-copy-images-to: 项目细节架构图
---

# Hive on Spark环境搭建

## Hive on Spark编译

版本:Hive:3.1.2		Spark:2.4.5

**如何去编译HIve实现Hive on Spark**:https://cwiki.apache.org/confluence/display/Hive/Hive+on+Spark%3A+Getting+Started#:~:text=Version%20Compatibility%20%20%20%20Hive%20Version%20,%20%201.6.0%20%204%20more%20rows%20

1）从官网下载Spark源码并解压

下载地址: https://www.apache.org/dyn/closer.lua/spark/spark-2.4.5/spark-2.4.5.tgz

2）上传并解压spark

3）进入spark解压后的目录

4）执行编译命令

```shell
[atguigu@hadoop102 spark-2.4.5]$ ./dev/make-distribution.sh --name without-hive --tgz -Pyarn -Phadoop-3.1 -Dhadoop.version=3.1.3 -Pparquet-provided -Porc-provided -Phadoop-provided
```

- --name xxx 这就是最后我们编译出来的包的名字
- --tgz 最终包格式是.tgz
- --Pyarn Spark on yarn运行模式
- -Phadoop-3.1 带Hadoop依赖
- -Dhadoop.version=3.1.3 -D是附带参数会覆盖源码中pom中Hadoop的版本号
- -Pparquet-provided -Porc-provided -Phadoop-provided provided表示在编译的时候会加载依赖,但是在打包的时候并不把这些依赖打入其中

5）等待编译完成，spark-2.4.5-bin-without-hive.tgz为最终文件

## Hive on Spark配置

1）解压spark-2.4.5-bin-without-hive.tgz

```shell
[atguigu@hadoop102 software]$ tar -zxf /opt/software/spark-2.4.5-bin-without-hive.tgz -C /opt/module
[atguigu@hadoop102 software]$ mv /opt/module/spark-2.4.5-bin-without-hive /opt/module/spark
```

2）配置SPARK_HOME环境变量

```shell
[atguigu@hadoop102 software]$ sudo vim /etc/profile.d/my_env.sh
```

添加如下内容

```shell
export SPARK_HOME=/opt/module/spark
export PATH=$PATH:$SPARK_HOME/bin
```

source 使其生效

```shell
[atguigu@hadoop102 software]$ source /etc/profile.d/my_env.sh
```

3）配置spark运行环境

```shell
[atguigu@hadoop102 software]$ mv /opt/module/spark/conf/spark-env.sh.template /opt/module/spark/conf/spark-env.sh
[atguigu@hadoop102 software]$ vim /opt/module/spark/conf/spark-env.sh
```

添加如下内容

```shell
export SPARK_DIST_CLASSPATH=$(hadoop classpath)
```

4）连接sparkjar包到hive，如何hive中已存在则跳过

```shell
[atguigu@hadoop102 software]$ ln -s /opt/module/spark/jars/scala-library-2.11.12.jar /opt/module/hive/lib/scala-library-2.11.12.jar
[atguigu@hadoop102 software]$ ln -s /opt/module/spark/jars/spark-core_2.11-2.4.5.jar /opt/module/hive/lib/spark-core_2.11-2.4.5.jar
[atguigu@hadoop102 software]$ ln -s /opt/module/spark/jars/spark-network-common_2.11-2.4.5.jar /opt/module/hive/lib/spark-network-common_2.11-2.4.5.jar
```

5）新建spark配置文件

```shell
[atguigu@hadoop102 software]$ vim /opt/module/hive/conf/spark-defaults.conf
```

添加如下内容

```shell
spark.master                                    yarn
spark.eventLog.enabled                          true
spark.eventLog.dir                              hdfs://hadoop102:8020/spark-history
spark.driver.memory                             2g
spark.executor.memory                           2g
```

6）在HDFS创建如下路径

```shell
hadoop fs -mkdir /spark-history
```

7）上传Spark依赖到HDFS

```shell
[atguigu@hadoop102 software]$ hadoop fs -mkdir /spark-jars

[atguigu@hadoop102 software]$ hadoop fs -put /opt/module/spark/jars/* /spark-jars
```

8）修改hive-site.xml

```shell
  <!--Spark依赖位置-->
  <property>
    <name>spark.yarn.jars</name>
    <value>hdfs://hadoop102:8020/spark-jars/*</value>
  </property>
  
  <!--Hive执行引擎-->
  <property>
    <name>hive.execution.engine</name>
    <value>spark</value>
  </property>
```

## Hive on Spark 测试

1）启动hive客户端

2）创建一张测试表

```sql
hive (default)> create external table student(id int, name string) location '/student';
```

3）通过insert测试效果

```sql
hive (default)> insert into table student values(1,'abc');
```

![1608135365580](C:\Users\monster\Desktop\work\Huskie\Huskie全域数仓\项目细节架构图\1608135365580.png)

## Yarn容量调度器队列配置

1）增加hive队列

默认Yarn的配置下，容量调度器只有一条Default队列。在capacity-scheduler.xml中可以配置多条队列，**修改**以下属性，增加hive队列。

```sql
<property>
    <name>yarn.scheduler.capacity.root.queues</name>
    <value>default,hive</value>
    <description>
      The queues at the this level (root is the root queue).
    </description>
</property>
<property>
    <name>yarn.scheduler.capacity.root.default.capacity</name>
<value>50</value>
    <description>
      default队列的容量为50%
    </description>
</property>
同时为新加队列添加必要属性：
<property>
    <name>yarn.scheduler.capacity.root.hive.capacity</name>
<value>50</value>
    <description>
      hive队列的容量为50%
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.user-limit-factor</name>
<value>1</value>
    <description>
      一个用户最多能够获取该队列资源容量的比例
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.maximum-capacity</name>
<value>80</value>
    <description>
      hive队列的最大容量
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.state</name>
    <value>RUNNING</value>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.acl_submit_applications</name>
<value>*</value>
    <description>
      访问控制，控制谁可以将任务提交到该队列
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.acl_administer_queue</name>
<value>*</value>
    <description>
      访问控制，控制谁可以管理(包括提交和取消)该队列的任务
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.acl_application_max_priority</name>
<value>*</value>
<description>
      访问控制，控制用户可以提交到该队列的任务的最大优先级
    </description>
</property>

<property>
    <name>yarn.scheduler.capacity.root.hive.maximum-application-lifetime</name>
<value>-1</value>
    <description>
      hive队列中任务的最大生命时长
</description>
</property>
<property>
    <name>yarn.scheduler.capacity.root.hive.default-application-lifetime</name>
<value>-1</value>
    <description>
      default队列中任务的最大生命时长
</description>
</property>
```

2）配置hive客户端任务提交到hive队列

为方便后续hive客户端的测试和shell脚本中的任务能同时执行，我们将hive客户端的测试任务提交到hive队列，让shell脚本中的任务使用默认值，提交到default队列。

每次进入hive客户端时，执行以下命令

```sql
hive (default)> set mapreduce.job.queuename=hive;
```

