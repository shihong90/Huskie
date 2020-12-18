#  Presto安装

##  **Presto** Server安装

**1）官网地址**

https://prestodb.github.io/

**2）下载地址**

https://repo1.maven.org/maven2/com/facebook/presto/presto-server/0.196/presto-server-0.196.tar.gz 

**3）将presto-server-0.196.tar.gz导入hadoop102的/opt/software目录下，并解压到/opt/module目录**

```shell
[atguigu@hadoop102 software]$ tar -zxvf presto-server-0.196.tar.gz -C /opt/module/
```

**4）修改名称为presto**

```shell
[atguigu@hadoop102 module]$ mv presto-server-0.196/ presto
```

**5）进入到/opt/module/presto目录，并创建存储数据文件夹**

```shell
[atguigu@hadoop102 presto]$ mkdir data
```

**6）进入到/opt/module/presto目录，并创建存储配置文件文件夹**

```shell
[atguigu@hadoop102 presto]$ mkdir etc
```

**7）配置在/opt/module/presto/etc目录下添加jvm.config配置文件**

```shell
[atguigu@hadoop102 etc]$ vim jvm.config
```

添加如下内容

```shell
-server
-Xmx16G
-XX:+UseG1GC
-XX:G1HeapRegionSize=32M
-XX:+UseGCOverheadLimit
-XX:+ExplicitGCInvokesConcurrent
-XX:+HeapDumpOnOutOfMemoryError
-XX:+ExitOnOutOfMemoryError
```

**8）Presto可以支持多个数据源，在Presto里面叫catalog，这里我们配置支持Hive的数据源，配置一个Hive的catalog**

```shell
[atguigu@hadoop102 etc]$ mkdir catalog

[atguigu@hadoop102 catalog]$ vim hive.properties 
```

添加如下内容

```shell
connector.name=hive-hadoop2
hive.metastore.uri=thrift://hadoop102:9083
```

**9）将hadoop102上的presto分发到hadoop103、hadoop104**

```shell
[atguigu@hadoop102 module]$ xsync presto
```

**10）分发之后，分别进入hadoop102、hadoop103、hadoop104三台主机的/opt/module/presto/etc的路径。配置node属性，node id每个节点都不一样。**

```shell
[atguigu@hadoop102 etc]$vim node.properties

node.environment=production
node.id=ffffffff-ffff-ffff-ffff-ffffffffffff
node.data-dir=/opt/module/presto/data


[atguigu@hadoop103 etc]$vim node.properties

node.environment=production
node.id=ffffffff-ffff-ffff-ffff-fffffffffffe
node.data-dir=/opt/module/presto/data


[atguigu@hadoop104 etc]$vim node.properties

node.environment=production
node.id=ffffffff-ffff-ffff-ffff-fffffffffffd
node.data-dir=/opt/module/presto/data
```

**11）Presto是由一个coordinator节点和多个worker节点组成。在hadoop102上配置成coordinator，在hadoop103、hadoop104上配置为worker。**

**（1）hadoop102上配置coordinator节点**

```
[atguigu@hadoop102 etc]$ vim config.properties
```

添加内容如下

```shell
coordinator=true
node-scheduler.include-coordinator=false
http-server.http.port=8881
query.max-memory=50GB
discovery-server.enabled=true
discovery.uri=http://hadoop102:8881
```

**（2）hadoop103、hadoop104上配置worker节点**

```shell
[atguigu@hadoop103 etc]$ vim config.properties
```

添加内容如下

```shell
coordinator=false
http-server.http.port=8881
query.max-memory=50GB
discovery.uri=http://hadoop102:8881
```

```shell
[atguigu@hadoop104 etc]$ vim config.properties
```

添加内容如下

```
coordinator=false
http-server.http.port=8881
query.max-memory=50GB
discovery.uri=http://hadoop102:8881
```

**12）在hadoop102的/opt/module/hive目录下，启动Hive Metastore，用atguigu角色**

```
[atguigu@hadoop102 hive]$
nohup bin/hive --service metastore >/dev/null 2>&1 &
```

**13）分别在hadoop102、hadoop103、hadoop104上启动Presto Server**

（1）前台启动Presto，控制台显示日志

```shell
[atguigu@hadoop102 presto]$ bin/launcher run

[atguigu@hadoop103 presto]$ bin/launcher run

[atguigu@hadoop104 presto]$ bin/launcher run
```

（2）后台启动Presto

```shell
[atguigu@hadoop102 presto]$ bin/launcher start

[atguigu@hadoop103 presto]$ bin/launcher start

[atguigu@hadoop104 presto]$ bin/launcher start
```

13）日志查看路径/opt/module/presto/data/var/log

## Presto命令行Client安装

**1）下载Presto的客户端**

<https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/0.196/presto-cli-0.196-executable.jar>

**2）将presto-cli-0.196-executable.jar上传到hadoop102的/opt/module/presto文件夹下**

**3）修改文件名称**

```shell
[atguigu@hadoop102 presto]$ mv presto-cli-0.196-executable.jar  prestocli
```

**4）增加执行权限**

```shell
[atguigu@hadoop102 presto]$ chmod +x prestocli
```

**5）启动prestocli**

```shell
[atguigu@hadoop102 presto]$ ./prestocli --server hadoop102:8881 --catalog hive --schema default
```

**6）Presto命令行操作**

```shell
Presto的命令行操作，相当于Hive命令行操作。每个表必须要加上schema。

例如：

select * from schema.table limit 100
```



##  Presto可视化Client安装

**1）将yanagishima-18.0.zip上传到hadoop102的/opt/module目录**

**2）解压缩yanagishima**

```
[atguigu@hadoop102 module]$ unzip yanagishima-18.0.zip

cd yanagishima-18.0
```

**3）进入到/opt/module/yanagishima-18.0/conf文件夹，编写yanagishima.properties配置**

```
[atguigu@hadoop102 conf]$ vim yanagishima.properties

	添加如下内容

jetty.port=7080

presto.datasources=atguigu-presto

presto.coordinator.server.atguigu-presto=http://hadoop102:8881

catalog.atguigu-presto=hive

schema.atguigu-presto=default

sql.query.engines=presto
```

**4）在/opt/module/yanagishima-18.0路径下启动yanagishima**

```shell
[atguigu@hadoop102 yanagishima-18.0]$

nohup bin/yanagishima-start.sh >y.log 2>&1 &
```

**5）启动web页面**

http://hadoop102:7080 

看到界面，进行查询了。

**6）查看表结构**