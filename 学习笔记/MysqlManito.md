##### 为什么要设计索引?

类似字典里面的编码精准的定位到某一个汉字的位置

在业务场景下表可能成百万级,想要快速的从里面定位到一条数据,这就要靠我们的索引系统了

##### 索引是什么?

where,关联,order by group by 这些操作后面的字段放入到B+树中,利用树的高效性,可以很快的帮我们排序,分组,定位范围,最后将包含索引数据的相关数据行进行返回

索引是表的目录，是数据库中专门用于帮助用户快速查询数据的一种数据结构。类似于字典中的目录，查找字典内容时可以根据目录查找到数据的存放位置，以此快速定位查询数据。**对于索引，会保存在额外的文件中**。 

索引是一种单独的、物理的对数据库表中的一列或多列的值进行排序的一种存储结构,他是某个表中一列或若干列的集合和相应的指向表中的物理标识这些值的数据页的逻辑清单.索引的作用相当于图书的目录

数据库通过索引找到特定值,然后顺指针找到该值对应的行

索引**是一个排序的列表**，在这个列表中存储着索引的值和包含这个值的数据所在行的物理地址，在数据十分庞大的时候，索引可以大大加快查询的速度，这是因为使用索引后可以不用扫描全表来定位某行的数据，而是先通过索引表找到该行数据对应的物理地址然后访问相应的数据。 

索引文件的结构:B+树

##### 如果我给表里面几个字段建立索引,只要写SQL语句就要用到索引么?



##### 如果是你,会如何设计索引?

数据库里面的数据一定是最后落地到磁盘,一定是一个文件,文件里面会有一个类似指针的东西

Hive创建索引就是按照1.关键字:key 2.文件名称 3.偏移量:offset 来创建索引文件的,这种创建的问题就是慢,Mysql属于OLTP数据库,对数据查询的即时性是有很高的要求的.所以Mysql将索引文件就替换成了B+数这种数据结构

在Mysql中除了使用B+数还使用到了Hash索引

##### 什么叫存储引擎?

不同的数据文件在物理磁盘上的组织形式

```
-- 展示Mysql的存储引擎
show  engines
```

最重要的:InnoDB、MyISAM、MEMORY

我们在聊索引的时候一定是不能脱离存储引擎的,像InnoDB和MyISAM使用的是B+树

而MEMORY使用的Hash

##### InnoDB支持Hash?

InnoDB他是自适应索引,用户是没办法进行干预的

xxx.frm表示当前存储的数据结构

xxx.ibd事件存储的数据文件,包含真实数据还包含索引

xxx.MYD表示Data数据

xxx.MYI表示Index索引

##### 什么是磁盘预读?

局部性原理:程序在一段时间内,仅使用其中一个小部分称为空间句部性

最近访问过的程序代码和数据,很快又被访问的可能性很大这个称为时间局部性

我们的数据最终都会放到我们磁盘里面,但是我们在进行实际的读取过程中,还是要将磁盘中的数据加载到内存,我们到内存当中去读数据.磁盘和内存的读写速度是相差很大的,磁盘预读的长度为dataPage数据页一般是4k/8k,主存和磁盘以页为单位交互数据,我以什么样的数据结构能每次以4k/8k的整数倍来读取这个数据

##### 设计索引的时候使用什么数据结构?

##### Mysql是如何实现的?



##### Hash有什么样的缺点导致Mysql不选择他?

1.Hash碰撞:比如我们取模8,他就会导致有很多相同的值,存储通过拉链,在一个Hash值下,当数据又是随机就会导致某一条链里面数据非常的长,因为这种原因我们在使用Hash表的时候需要设计一个非常完美的Hash算法(扰动函数)

2.存储HashMap的时候,当我存完里面的数据后,我取的时候要不要把Hashmap里面整体的数据都放到内存里面?

肯定是要放入到内存的,HashMap是要读过文件的.而且HashMap最大容量2>>31,数据量很大,如果将这些数据都放入到我们的内存,很占用我们的内存空间

3.Hash进行值的查找是通过key来寻找Val,如果是等值查询一定速度很快,如果做的东西是范围查询,一定很慢他要挨个进行对比,效率很低

##### 为什么Memory可以用Hash索引?

1.占内存他不怕

2.Hash算法比较麻烦,他设计一个相对而言比较公正的hash算法

3.保证都是等值查询,就算是范围查询他也不怕,反正都在内存,查询效率极高

##### 二叉树、BST二叉搜索树、红黑树、AVL平衡二叉树、B树为什么Mysql选择用B树

hash:等值查询可以,但是一旦范围查询等同于全表Scan

二叉树:会产生倾斜问题,退化变为链式,会导致树过深,导致io频繁

AVL树:在插入节点信息时会发生旋转操作,保证当前这棵树的最短子树跟最长子树高度之差不能超过1,数据越多就会产生越多次的数据旋转,而这个旋转会比较浪费时间,插入删除效率极低,而查询效率是比较高的,而且依然会产生树过深的问题

红黑树:最长子树只要不超过最短子树的两倍即可,旋转操作还在,但是增加了一个变色功能,而且依然会产生树过深的问题,而且还是二叉树的问题,节点有且仅有两个分支.

B树他的分支节点是无限制的,就直接避免了树深导致IO频繁的问题,但是他还是有问题,问题是一个Mysql读取的磁盘块16K本来就不大,它是由一部分真实数据(1条大致是占用1k空间)+指针所组成的,真实数据占用了绝大部分空间,导致指针的数量不够多,导致不足以支撑很大的存储数据量

B+树是在B树的基础上进行优化由原本的磁盘块存储数据+指针转为,磁盘块全部存储指针,将所有的数据都放到叶子节点

所有的数据是要落地到磁盘里面,当落地磁盘时,每一次从磁盘取一个数据块(页)4K回来,Mysql的InnoDB这种存储引擎一次默认是取16k(4页)的数据,16k的数据如果使用红黑树,每一个叶子节点放一个值,要放很多的节点,而且节点越多,就会造成你整个树的深度,无限加深,当树越深就会产生IO太频繁的问题,磁盘跟内存在做交互的时候你需要n多次的访问,磁盘读写速度是固定的,要想提高你整个IO的效率,只有两种途径:1.减少IO的次数;2.减少IO的大小.**归根到底还是因为IO的问题导致mysql选择使B+树**

##### 为什么B树就可以减少IO的次数?

因为B树/B+树他放的不是一个值,而是值的集合

##### Mysql里面的B+树到底是3层还是4层?

3层和4层这个东西是取决于当前的一个数据量的

##### 前缀索引什么意思?

我们有一个数据表citydemo他有一个字段city他是Varchar(50)的长度,我在实际做的时候可以将这个50截取一部分,

##### 什么是聚簇索引什么叫非聚簇索引?

是你数据是否和你的索引存放在一起的,InnoDB用的就是聚簇索引,而MyISAM 用的就是非聚簇索引

##### B+树的查找方式?

1.从根节点开始的随机查找

2.基于双向链表的范围查找和分页查找

##### 推荐使用主键自增?

如果你的数据库是分布式的不建议使用

如果是单机的非常建议使用主键自增

##### B树

存储空间=16 * 16 * 16

##### B+树

我们B树-->B+树的演化就是我们只有叶子节点是实际的数据,第一层第二次全部都是指针和主键值,这样子就大大的提高了存储的空间

存储空间1600 * 1600 * 16=40960000 

##### InnoDB与MyISAM的区别?

InnoDB与Mysum都是使用的B+树

但是区别是InnoDB叶子节点存储的是实际的整行数据,而MyISAM叶子节点放置的是一个地址,根据地址去读取数据所在行

##### 回表是什么意思?

在InnDB中会默认为我们的主键建立索引,有些情况下我们还会为我们的普通列创建索引,普通列的最后节点的叶子节点里面存储不是整行数据而是主键值,然后我们在通过主键值到主键这颗B+树里面定位到其叶子节点的数据,也就是查了2个B+树,故称为回表

##### 索引覆盖是什么?

如果我每次查数据的时候都需要先查这个值,都需要回表查询两个B+树,这样会增大IO.而当我们进行select id from table.这时候当你查的是id,就避免了回表的发送,这就叫做索引覆盖

##### 索引的优点是什么?

1.大大减少了服务器需要扫描的数据量

2.帮助服务器，避免order by 全排序,我们要通过索引进行排序,因为索引已经排好序了

3.将随机IO变成顺序IO

##### union和union all区别是什么,那个会更好一点?

建议选择unill all,union默认是包含一个distinct操作 

##### 索引的用处是什么?

1.快速查询匹配where子句的行,我们将我们索引列变成where后的条件行

2.从consideration中消除行,如果可以在多个索引之间进行选择,mysql通常会使用找到最少行的索引

3.如果表具有多列索引,则优化器可以使用索引的任何最左前缀来查找行

4.当表连接的时候,从其他表检索行数据

5.查找特定索引列的min或max值

6.如果排序或分组时可以在用索引的最左前缀上完成的,则对表进行排序和分组

7.在某些情况下,可以优化查询以检索值而无需查询数据行 

#####  索引下推

select * from table where name=? and age=?

数据存储在磁盘,而mysql是通过服务跟磁盘发生交互

没有索引下推:

- 1.先从存储引擎中根据name筛选拉取数据
- 2.在mysql server服务层根据age进行数据的筛选


有索引下推:

- 1.会在拉取数据的时候直接根据name,age来获取数据
- 2.不需要在server服务层做任何的数据筛选

所谓的下推就是原本根据一个字段将数据拉回来,在通过其他字段进行筛选,现在直接通过所有字段进行筛选后直接返回结果,由原来的server服务层推到存储引擎层直接进行筛选

原来的时候只能通过索引去匹配行记录,去行记录里面在进行一个数据查找,现在换另一种方式

我在找到索引那个id值的时候,就直接把这个行给过滤掉了

##### 页分裂和页合并的过程?

##### 索引下推的优点

没用索引下推IO量是比较大的,速度会比较慢,将原本的内存筛选,下压到磁盘筛选,从而减少宝贵内存资源占用.

索引下推唯一的缺点是需要在磁盘上多做数据筛选,原本的筛选是放在内存中的,现在放到了磁盘查找数据的环节,这样做看起来成本比较高,但是别忘了,数据是排序的,所有数据是聚集存放的,所以性能不会有影响,而且整体的IO量会大大减少,反而会提升性能

##### 为什么刚开始的时候没有使用索引下推?

刚开始磁盘性能不足,而且现在并发任务会比较多,如果不足索引下推全部走内存,那是无法支撑起高并发场景的

##### 索引分类

- 1.主键索引
- 2.唯一索引
- 3.普通索引:辅助索引/二级索引
- 4.全文索引
- 5.组合索引

##### 什么是最左匹配?

必须先有最左边,才会去看右边,右边可有可无

以最左边的为起点任何连续的索引都能匹配上**。同时**遇到范围查询(>、<、between、like)就会停止匹配

```SQL
示例:最左匹配是与组合索引一起一起使用的

组合索引:name,age

where name=? and age=? 走索引

where name=? 走索引

where age = ? 不走索引

where age=? and name=? 走索引

建索引:alter table test add index idx_test(name,age);
```

查询是否走索引:explain select * from test where name = 'zhangsan' and age =10

给当前表添加一个Share锁,不会有创建临时文件的资源消耗,还是在源文件中,但是此时有人发起DML操作,很明显数据会不一致,所以添加Share锁,读取是没有问题的,但是DML操作会有问题.

数据操纵语言DML主要有三种形式：

 1) 插入：INSERT 2) 更新：UPDATE 3) 删除：DELETE  

索引分类:

1.主键索引:关系到了数据的组织形式

2.唯一索引:这就意味着当前列是不能有重复值出现的

3.普通索引:也叫二级索引/辅助索引,给主键或者唯一键外的列创建索引叫普通索引

4.全文索引:一般不用直接使用ES

5.组合索引

key_len:索引的长度



列值允许为null要加一个字节

##### 有了索引我们将如何进行一个数据的查找?

##### 索引长度如何进行计算?

int 类型占4字节

varchar类型是3个字节  

例如:varchar(24)和varchar(20)  --> (24+20)*3 = 132个字节

如果是varchar类型还要最后额外加2个字节

#### 如果你发现一个sql运行比较慢,我应该如何去创建索引?

##### 如何将一个.sql文件执行?

1.将.sql文件上传至linux中

2.使用Mysql的source

```sql
source /root/sakila-schema.sql
```



#### 在写sql的时候有哪些点可能会造成索引失效?

##### 情况一 匹配列前缀:可以匹配某一列的值的**开头部分**

```SQL
explain select * from staffs where like 'J%' 走索引

explain select * from staffs where like '%J%'不走索引
```

为什么前面加了一个%号就索引失效了那?

因为%代表任意字符,故而所有的东西都能匹配上,所以索引就失效了

当J%是表示已J开头后面有多少个那就不管了

##### 情况二 匹配范围值:可以查找某一范围的数据

在使用组合索引的过程,如果中间的某一个环节出现了范围查找后续索引直接失效

explain select * from staffs where name = 'Mary ' and age>10 and pos=20;

##### or走不走索引分两种情况:

如果你是单列索引or是会走索引的:

比如:id主键索引,last_name普通索引就会走索引

如果是组合索引:

- 1.如果表全部列都是组合索引or是会走全部列对应的索引
- 2.如果部分列是组合索引or是不会走索引的

##### 当使用索引列进行查询的时候尽量不要使用表达式,把计算放到业务层而不是数据库层

#### 自然主键,代理主键?

代理主键:跟我当前业务系统无关的列做的主键

自然主键:表示跟当前业务系统挂钩的

建议使用代理主键

前缀索引 

##### 使用索引扫描来排序

索引本身是有序的,利用索引进行排序

##### 排序为什么浪费时间?

##### 隐式类型转换/强制类型转换会触发全表扫描

比如:我们创建一个表字段phone为 varchar(20),我们对其进行一个查询

select * from where phone ='12323232';   走索引

select * from where phone =12323232;     不走索引

这里涉及到一个点叫做:隐式类型转换

在比如说:当你进行两个表的关联,关联字段类型不一致,其实就等于增加了一个类型转换函数,就导致了索引失效,导致不走索引

##### 更新十分频繁,数据分区度不高的字段上不宜建立索引

如何确定当前字段区分度:当前类里面的唯一值数量/当前表里面的总行数>80% 可以建索引   如果小于80%不建议建索引

##### 当需要进行表连接的时候,最好不要超过三张表,因为需要join的字段,数据类型必须一致

##### 能使用limit的时候尽量使用limit

limit最严谨的解释是限制输出

单表索引字段不允许超过5个(组合索引)

创建索引的时候应该避免以下错误概念:

- 1.索引越多越好索引越多你所需要占的磁盘文件就越大,IO量就会越大
- 2.过早优化,在不了解系统的情况下进行优化

##### MRR是什么?

全称:mult_range read 

在内存中增加了一个排序环节

##### FIC是什么?

全称:fast index create 涉及数据的DML操作

我们在插入和删除数据的时候你需要修改我们对应的索引,在修改索引的过程是这样的一个过程:

​	1.先创建临时表,将数据导入到临时表

​	2.把原始表删除

​	3.修改临时表的名字

而有了FIC后,他是加了一个S锁share锁,不会有创建临时文件的资源消耗,还是源文件中,但是此时如果有人发起DML操作,很明显数据会不一致,所以添加Share锁,读取是没有问题的,但是DML会有问题

##### 什么是存储引擎?

不同的数据文件在磁盘的不同组织形式

##### innodb和myisam有什么区别?

1.innodb支持事务myisam不支持

2.innodb支持外键,myisam不支持

3.innodb支持表锁和行锁,但是myisam只支持表锁

4.innodb在5.6版本之后支持全文索引

5.innodb索引的叶子节点之间存储数据,而myisam存放地址

##### innodb这么好我为什么还要用myisam?

myisam是mysql原生的存储引擎,而innodb是其他公司开发的类似插件似的存储引擎,myisam最大的优点在于统计总数

##### Calacie

标准的SQL解析引擎

##### 索引要不要存储在磁盘?

一定是要的,他如果要存磁盘,所以他也是要占用磁盘空间的.

##### 基本的操作系统常识

1.局部性原理:数据和程序都有聚集成群的倾向,分为空间局部性和时间局部性

2.磁盘预读:当我们的内存跟磁盘发送交互的时候,他不可能说一个字节一个字节的来读取,因为这样的话效率是在是太低了.最好的方式是一次读一块,把他相邻空间的数据一次都给读取回来.

内存跟磁盘在进行交互的时候要保证每次读取需要一个逻辑单位,而这个逻辑单位叫做页或者叫dataPage,一般都是4k或者8k,在进行读取的时候一般都是4k的整数倍.

innodb每次读取16kb的数据

##### 什么叫空间局部性什么叫时间局部性?

空间局部性:经常被查询的数据可能是在一起被存放的		

时间局部性:之前被查过的数据很有可能再次被查询

memory存储引擎使用Hash索引同时innodb支持自适应hash

##### B+树与其他树相比优势是什么?

1.他是多叉树

2.节点有序

3.是一颗平衡树

4.每一个节点可以存储多条记录

##### 每个磁盘块都是16KB那么我的一个4层B+树如果我要读15条记录出来,我要读多少个字节?

##### Mysql的索引一共有几层?

一般情况下3-4层就足以支持千万级别表的查询

##### 创建索引的字段是长了好还是短了好?

短了好,原因是在层数不变的情况下可以存储更多的数据量

##### 什么叫代理主键什么是自然主键?

所谓的代理主键就是和业务无关列,自然就是和业务相关

##### 我们在创建表的时候是用代理主键还是自然主键?

能使用代理主键尽量多的使用代理主键

##### 主键设置好之后,要不要自增?

在满足业务的情况下尽可能自增,不自增会增加索引的维护成本

##### 在分布式应用场景中,自增id还适用么?

不适用,用雪花算法snowflake,或者自定义id生成器



行式存储-->Text、sequence file

列式存储-->ORC、parquet

##### 什么是基数(hyperloglog)?

一个100行的表,name列有80个唯一值,那么基数就是80

##### mysql事务四大特性

1.原子性:要么都成功要么都失败

2.一致性

3.隔离性

4.持久性

##### 原子性的实现原理是什么?

undo log:保存的是跟执行操作相反的操作

##### 隔离性的实现原理是什么?

锁

读未提交:会引发脏读、幻读、不可重复读

读已提交:会引发不可重复读、幻读

默认隔离级别:可重复读-->会引发幻读操作

串行化:最慢的

隔离级别越低,效率约高,越不安全;

隔离级别越高,效率越低,越安全

Mvcc :Multi Version Concurrency Control多版本并发控制

##### 读已提交和可重复读这两个隔离级别最容易混淆

读已提交会产生一个问题,不可重复读问题

##### 持久性的实现原理是什么?

redo log :回滚日志、持久日志 ->为了保证crash safe :数据一致性

如果发生异常情况,就是数据没有持久化成功,只要日志持久化成功了,依然可以进行恢复

这种机制最根本的保证是WAL:Writer Ahaead Log预写日志,不管你的实际数据是否保存成功,最终只要我的日志保存成功了,我就认为我的数据保存成功了

除了我们的redo log之外还有一个binlog:二进制文件

redo log、udo log全部归属于innodb存储引擎

而binlog归属于Mysql的Server

binlog于redo log在进行操作的时候会有一个名词叫:二阶段提交-->组提交

先执行我们的redo log会将其置于一个prepare的阶段-->去写我们的binlog-->将我们的redo log置于commit

其实在binlog中也可以拆解成两个过程:1.perpare 2.commit

##### 一致性非锁定读、一致性锁定读是什么意思?

```SQL
select * from table where id =1 for update;--一致性锁定读,加了一个排他锁
--如果你查询的时候只做这个一个操作
select * from table where id =1--一致性非锁定读
如果你有多个并发事务进行操作的时候,他依然会有一个相关的问题

```

##### 什么是不可重复读?

在同一个事务中执行相同的SQL语句结果不同的

##### 为什么可重复读时不会出现而读已提交时会出现在同一个事务中执行相同的SQL语句结果不同的?

多个不同并发事务操作会涉及多个版本的问题v1-->v2-->v3  [V1V2V3是一个数据行的不同版本变化]

当我到V2之后开启了一个新的事务:重复查询,到v3之后我又开启了一个新的事务:修改记录

V2能否读到一样的结果取决于V2事务开启的时候锁定的数据是哪一个版本,读已提交读取的是最新的一致性的快照版本也就是V3,而可重复读读取的是事务开启之前的版本也就是V1

当你当前多个事务并发执行的时候,他是会记录你的p_id的,还会记录你之前事务的p_id值

##### 啥是脏读?

脏读是读取到你未提交的数据

##### 低水位、高水位?

##### 什么叫基数Cardinality统计?

某一个列去重之后唯一值的个数

##### 什么叫做主从复制?

保证两台机器的数据同步

##### 延迟怎么解决?

通过组提交来解决的

##### Mysql会不会丢数据?

##### 所有的持久化引擎,会不会丢失数据?

99.9%数据不丢失,每人敢说100%不丢失,我们只能说用尽可能多的机制来保证我们的数据尽可能的不丢失

乐观锁悲观锁

调优

索引

MVCC

存储引擎

事务

主从复制

读写分离

分库分表

锁

日志系统

##### 性能监控

性能监控的意义:Mysql执行非常非常慢,很重要的一点是你要知道具体是哪一个步骤慢了

mysql分三个层次:

- client客户端
- mysql所提供的server层
- 存储引擎层:innoDB:磁盘、MyISAM:磁盘、memory内存

首先用户向我们mysql的server发送一个连接请求,server这会有个服务叫连接器,提供连接服务,会做一个权限验证用户名和密码进行匹配.当验证成功后用户发送SQL请求,我们发送的sql就会进入分析器,将写的SQL语句字符串进行关键字切分,将切分后的语句进行成词法分析、语法分析,最形成ATS树:抽象语法树,在运行SQL的时候有很多种方式,比如a and b,是先运行a那还是先运行b那,就涉及到了优化器,优化器的优化分两种方式:1.RBO:基于规则的优化 2.CBO:基于成本的优化.优化完成后就要进入执行器,执行器:跟我们的存储引擎进行挂钩,从磁盘中获取某些数据了,但是取数据的时候是一定不能写select *的,这是因为IO问题,IO问题硬件层面是没办法避免的:1.减少IO的一个量 2.减少IO的一个次数.

一个SQL语句的执行比较慢,我们是要看具体是那个步骤慢了,

set profiling=1;

```SQL
show profile all; --我们通过这个语句就可以看到当前SQL运行的每一个步骤的精确的所消耗时长,以及所消耗的系统资源
```

mysql8之后找到了更好的方式,来替代show profiling

Performance它提供了更加复杂的机制来监控我们的mysql,默认情况下是开启的,如果想要关闭使用set performance_schema =off 是关闭不了的,需要进入配置文件进行修改

instruments生产者,用于采集mysql中各种各样的操作产生的事件信息,对应配置表在的配置项我们可以称为监控采集配置项

consuments消费者:对应的消费者表用于存储来自instruments采集的数据,对应配置表中的配置项我们可以称为消费存储配置项



**没有\G是按照表进行展示,有\G按照文本展示**
查看我们mysql服务数据库连接情况:show processlist;

##### 什么索引下推?

原来的时候我只能通过索引去匹配行记录,去行记录里面在进行一个数据数据查找,现在我直接找到索引行的那个id值的时候直接将他过滤掉了

##### schema与数据类型优化

数据类型的优化:
	更小的通常更好
		应该尽量使用可以正常存储数据的最小数据类型,更小的数据类型通常更快,因为他们占用更少的磁盘、内存和cpu缓存,并且处理时需要的cpu周期更少,但是需要确保没有低估需要存储的值的范围,如果无法确定哪个数据类型,就选择你认为不会超过范围的最小类型
	进入我们的Mysql/MySQL Server/data下就是存储我们的数据表位置:
	innoDB引擎:
		表名.frm 存储的是表结构
		表名.ibd 存储的是是具体的数据文件
	如果换成MySysm引擎:
		表名.frm 存储的是表结构
		表名.MYD 数据文件
		表名.MYI 索引文件 
	简单就好
		简单数据类型的操作通常需要更少的cpu周期,例如:
		1.整形比字符串材质代价更低,因为字符集合和校对规则是字符比较比整形比较更复杂
		2.使用Mysql自建类型而不是字符串来存储日期和时间
		3.用整形存储IP地址
		我们想要存储一个字符串ip地址:'192.168.85.111',他是占用比较大空间的,我们在存储的时候可以将其转换为int类型进程存储
		字符串转整型-->select inet_aton('192.168.85.111')
		整型转字符串-->select inet_ntoa(3232257391)
	尽量避免null
		在数据里面null是否等于null?
			null是不等于null的
		如果在查询中包含可为null的列,对mysql来说是很难进行优化的,因为可为null的列使的索引、索引统计和值比较都更加复杂,坦白来说,通常情况下null的列改为not null带来的性能提升比较小,索引没有必要将所有的表的schema进行修改,但是应尽量避免设计成可为null的列
	实际细则
		整数类型
			可以使用的机制整数类型:tinyint、smallint、mediumint、int、bigint分别使用8,16,24,32,64位存储空间
			尽量使用最小的存储类型
		字符和字符串类型
			char:固定长度的字符串
				1.最大长度255
				2.会自动删除末尾的空格
				3.检索效率、写效率,会比varchar高,以空间换时间
				应用场景:
					1.存储长度波动不大的数据,如:md5摘要
					2.存储短字符串、经常更新的字符串
			varchar:根据实际内容长度保存数据(常用)
				1.使用最小的符合需求的长度
				2.varchar(n)n小于等于255使用额外一个字节保存长度,n>255使用额外两个字节保存长度
				3.varchar(5)与varchar(255)保存统一的内容,硬盘存储空间相同,但内存占用不同,是指定的大小
				4.varchar在mysql5.6之前变更长度,或者255一下变更到255以上时,都会导致锁表
			用于场景:
				1.存储长度波动大的数据,如:文章,有的会很短有的会很长
				2.字符串很少更新的场景,每次更新后都会重算并使用额外存储空间保存长度
				3.适合保存多字节字符,如:汉字,特殊字符等
			blob和text
				mysql把每个bolb和text值当中一个独立的对象处理.两者都是为了存储很大数据而设计的字符串类型,分别采用了二进制和字符串方式存储 
				几乎没人用,因为如果你这个数据非常大,我们一般会将其写成一个文件,将文件的地址存储在mysql中

	datetime、timestamp和date
		datatime
			占用8个字节
			时间范围:1000-01-01-9999-12-31
			与时区无关,数据库底层时区配置,对datetime无效
			可以保存到毫秒
			课保存时间范围大
			不要使用字符串存储时间类型,占用空间大,损失日期类型函数的便捷性
		timestamp(常用,秒进本上是够了)
			占用4个字节
			时间范围:1970-01-01到2038-01-19
			精确到秒
			采用整型存储
			依赖数据库设置的时区
			自动更新timestamp列的值
		date
			占用的字符数比使用字符串、datetime、int存储要少,使用date类型只需要3个字节
			使用date类型还可以利用日期时间函数进行日期之间的计算
			date类型用于保存1000-01-01到999-12-31之间的日期
	
	使用枚举类型替代字符串类型
		有时可以使用枚举类代替常用的字符串类型,mysql存储枚举类型会非常紧凑,会根据列表值的数据压缩到一个或两个字节中,mysql在内部会将每个值在列表中的位置保持为整数,并且在表的.frm文件中保存'数字-字符串'映射关系的查找表
	
		create table enum_test(e enum('fish','appke','dog')not null);
		inset into enum_test(e) values('fish'),('dog'),('apple');
		--通过+0我们就可以看出来:虽然展示的是字符串,但是通过加0你可以看到其实他的本质是整型
		select e+0 from enum_test; 
	
	特殊类型数据
		人们常使用varchar(15)来存储地址,然而,他的本质是32位无符号整数不是字符串,可以使用inet_aton()和inet_ntoa函数在这两种表示方法之间转化
		案例:
			select inet_aton('1.1.1.1')
			select inet_ntoa(16843009)
合理使用范式和反范式

##### 三范式的目的是什么?

减少我们的数据冗余
	范式
		优点:
			范式化的更新通常比反范式要快
			当数据较好的范式化后,很少或者没有重复的数据
			范式化的数据比较小,可以放在内存中,操作比较快
		缺点:
			通常需要进行关联
			阿里规范:绝对不能三张表进行join,如果数据特别大,会非非常大
	反范式
		优点:
			所有的数据都在同一张表中,可以避免关联
			可以设计有效的索引
		缺点:表内的冗余较多,删除数据的时候会造成表有些有用的信息丢	  失
	注意
		在企业中很难能做到严格意义上的范式或者反范式,一般需要混合使用

主键的选择
	代理主键
		与业务无关的,无意义的数字序列
	自然主键
		事物属性中的自然唯一标识
	推荐使用代理主键
		他们不与业务耦合,因此更容易维护
		一个大多数表,最好是全部表,通用键
		要编写的源码数量,减少系统的总体拥有

字符集的选择

##### 存中文的时候是乱码,一堆问号是什么原因?

在mysql里面如果你设置的是utf8存储,你存储的只能是2个字符的中文,如果是3个字符就乱码了,所以建议设置为utf8b4
	1.纯拉丁字符能表示的内容,没必要选择latinl之外的其他字符编码,因为这会节省大量的内存空间
	2.如果我们可以确定不需要存放多种语言,就没必要非得使用utf8或者unicode字符类型,这会造成大量的存储空间浪费,如果你是其他字符就用utf8b4,其余就用拉丁就搞定了
	3.mysql的数据类型可以精准到字段,所以当我们需要大型数据库中存放多字节数据的时候,可以通过对不同表不同字段使用不同的数据类型来较大程度减少数据存储量,进而降低IO操作次数并提高缓存命中率

存储引擎的选择
	在my.ini文件中-->default-storage-engine=INNODB
	MymISAM:最大的问题就是没法进行持久化

| 索引类型     | MyISAM非聚簇索引 | InnoDB聚簇索引(数据文件和索引文件放在一起) |
| ------------ | ---------------- | ------------------------------------------ |
| 支持事务     | 否               | 是                                         |
| 支持表锁     | 是               | 是                                         |
| 支持行锁     | 否               | 是                                         |
| 支持外键     | 否               | 是                                         |
| 支持全文索引 | 是               | 是(5.6后支持)                              |
| 适合操作类型 | 大量select       | 大量insert、delete、update                 |

##### 如何区分他到底是表锁、还是行锁?

InnoDB这种存储引擎默认情况下给索引来进行加锁

适当的数据冗余
	1.被频繁引用且只能通过join2张或者更多大表的方式才能得到的独立小字段

	物化视图(mysql没有)
		我们定义一个SQL语句,每次用视图的时候提前将SQL语句执行一次,而物化视图会将SQL语句的执行结果提前放到一张物理表里面
	
	2.由于每次join仅仅只是为了取得某个小字段的值,Join到的记录又大,会造成大量不必要的IO,万全可以通过空间换取时间的方式来优化.不过,冗余的同时需要确保数据的一致性不会遭到破坏,确保更新的同时冗余字段也被更新

适当拆分
	当我们的表中存在类型于Text或者是很的varchar类型的大字段的时候,如果我们大部分访问这张表的时候都不需要这个字段,我们就该义无反顾的将其拆分到另外的独立表中,以减少常用的数据所占用的存储空间.这样做的一个明显好处就是每个数据块中可以存储的数据条数可以大大增加,既减少物料IO次数,也能大大提高内存中的缓存命中率

执行计划
	示例
		explain select * from user;
	id
		select查询的序列号,包含一组数组,表示查询中执行select子句或者操作表的顺序
		id号分为三种情况:
			1.如果id相同,name执行顺序从上到下
			2.如果id不同,如果是子查询,id的序号会递增,id值越大优先级越高,越先被执行
			3.id相同和不同的,同时存在:相同的可以认为是一组,从上往下顺序执行,在所有组中,id值越大,优先级越高,越先执行
	select_type
		主要用来分别查询的类型的,是普通查询还是联合查询还是子查询

	type
		以何种方式访问我们的数据
		效率从最好到最坏依次是:
			system表只有一行记录>
			const:这个表至多有一个匹配行>
			eq_ref:适用唯一性索引进行数据的查找>
			ref:使用了非唯一性索引进行数据的查找>
			ref_or_null对于某个字段即选用关联条件,也需要null值的情况下,查询优化器会选择这种访问方式>index_merge在查询过程中需要多个索引组合使用>
			unique_subquery该链接类型类似与index_sunquery,适用的是唯一索引>
			index_subquery利用索引来关联子查询,不在扫描全表>
			range表利用索引查询的时候限制了范围,在指定范围内进行查询,这样避免了index的全索引扫描,适用的操作符:=,<>,>,>=,<,<=,is null,between,like,or in()>
			index全索引扫描>
			ALL全表扫描
			一般情况下,得保证查询至少到达range级别,最好能达到ref
	possible_keys
		尽可能的显示这张表中的索引也就是:可能会用到的索引
	key
		实际应用的索引,如果为null,则没有使用索引,查询中若使用了覆盖索引,则该索引和查询的select字段重叠
	key_len
		表示索引中使用的字节数,索引越短越好,这样就能进行小的占用空间,从而减少IO的次数和IO的量
	ref
		显示索引的哪一列被使用了,一个const就是一列
	rows(预估值,不一定准确)
		根据表的统计信息及索引使用情况,大致估算出找出所需记录需要读取的行数,此参数很重要,直接反应的sql查找了多少数据,在完成目的的情况下越少越好
	Extra
		包含额外信息
			using filesort:说明mysql无法利用索引进行排序,只能利用	排序算法进行排序,会消耗额外的位置
			using temporary:建立临时表来保存中间结果,查询完成之后	把临时表

删除
	using index:当前查询的时候是否使用我们的覆盖索引
	using where:使用where进行添加过滤
	using join buffer:使用连接缓存
	impossible where:where语句的结果总是false不存在

##### 慢查询是什么?

慢查询有一个慢查询日志,当你开启这个日志后会,他会记录当前查询比较慢的SQL语句,让你可以进行一个相应优化

##### 如果让你来做索引这件事情,你会怎么来存储他?

索引就是一堆数据,如果你想定位某一个文件的一个位置,必须要知道当前数据在哪一个文件,当知道在那个文件后还要知道他在这个文件里面的offset偏移量.第二点我还需使用一个数据结构去存储这个数据

##### 什么是Hash索引?

基于哈希表的实现,只有精准匹配所有的列的查询才有效

在mysql中,只有mmeory的存储引擎显式支持哈希索引

哈希索引自身只需存储对应的hash值,所以索引的结构十分紧凑,这让哈性索引的查找速度非常快

##### Hash索引的限制是什么?

1.hash索引只能包含哈希值和行指针,而不存储字段值,索引不能使用索引中的值来避免读取行

2.哈希索引数据并不是按照索引值顺序存储的,所以无法进行排序

3.哈希索引不支持部分列匹配查找,哈希索引是使用索引列的全部内容来计算hash值

4.哈希索引支持等值比较查询,但是不支持任何范围查询

5.访问hash索引的数据非常快,除非有很多hash冲突,当出现哈希冲突的时候,存储引擎必须遍历链表中的所有行指针,逐行进行比较,直到找到所有符合条件的行

6.hash冲突比较多的话,维护的代价也会很高

##### 有哪些避免hash冲突的方法?

编写比较优秀的hash算法

扰动函数

##### Hash索引的案例

当需要存储大量的URL,并且根据URL进行索引查找,如果使用B+树,存储的内容就会很大

```sql
select id from url where url=''
```

也可以利用url,使用CRC32做哈希,将一个很长的字符串变成一个指定长度的字符串,可以使用以下查询方式:

```sql
select id from url where url='' and url_cre=CRC32('')
```

此查询性能较高原因是使用体积很小的索引来完成查找

CRC32:循环冗余校验

##### 组合索引的使用需要考虑的问题是是什么?

当包含多个列作为索引,需要注意的是正确的顺序依赖于该索引的查询,同时需要考虑如何更好的满足分组和排序的需要

```sql
案例:

建立组合索引abc

where a=3 and b=5	使用了a,b

where a=3 and c=4	使用了a

where a=3 and b>10 and c=7	使用了a,b
```

##### 聚簇索引优势是什么?

1.可以把相关数据保存在一起

2.数据访问更快,因为索引和数据保存在同一个树中

3.使用覆盖索引扫描的查询可以直接使用页节点中的主键值

##### 聚簇索引劣势是什么?

1.聚簇数据最大限度的提高了IO密集型应用的性能,如果数据全部在内存,那么聚簇索引就没有什么优势

2.插入速度严重依赖于插入顺序,按照主键的顺序插入是最快的方式

3.更新聚簇索引列的代价很高,因为会强制将每个被更新的行移动到新的位置

4.基于聚簇索引的表在插入新键,或者主键被更新导致需要移动行的时候,可能面临页分裂的问题

5.聚簇索引可能导致全表扫描变慢,尤其是行比较稀疏,或者由于页分裂导致数据存储不连续的时候

总的来说就是索引维护很麻烦

##### 聚簇索引,非聚簇索引区别在哪里?

聚簇非聚簇不是单独的索引类型,而是一种数据存储方式.指的是数据行跟相邻的键值紧凑的存储在一起,还是B+树存储的不是数据,而是数据地址,最后通过地址值找到数据

##### 场景题:将一堆很大的数据导入到mysql,你将怎么做?

当我们想要将一堆数据导到mysql中,先要将mysql默认的主键键索引给关闭,把数据先拉到mysql,在开启主键键索引,进行索引的创建,如果不这样做就会出现一边导数据一边键索引,这样做效率是极低的

#####  什么是覆盖索引?

1.如果一个索引包含所有需要查询的字段值,我们称之为覆盖索引

2.不是所有类型的索引都可以成为覆盖索引,覆盖索引必须要存储索引列的值

3.不同的存储实现覆盖索引的方式不同,不是所有的引擎都支持覆盖索引,memory不支持覆盖索引

##### 覆盖索引的优势是什么?

1.索引条目通常远小于数据行大小,如果只需要读取索引,那么mysql就会极大的减少数据的访问量

2.因为索引是按照列值顺序存储的,所以对于IO密集型的范围查询会比随机从磁盘读取每一行数据的IO要少的多

3.一下存储引擎如MyISAM在内存中只缓存索引,数据则依赖于操作系统来缓存,因此要访问数据需要一次系统调用,这可能会导致严重的性能问题

4.由于Innodb的聚簇索引,覆盖索引对Innodb表特别有用

##### 非关系数据库具有的特点是什么?

存储结构灵活没有固定的结构

对事务的支持比较弱,但对数据的并发处理性能搞,非常适合处理日志数据

大多不使用SQL语言操作数据,像是Mg使用的是js,hadoop使用mR

redis使用java

##### 什么是事务?

事务是数据库执行操作的最小逻辑单元

事务可以由一个SQL组成也可以由多个SQL组成

##### 事务的ACID

原子性A:一个事务中的所有操作,要么全部完成,要么全部不完成,不会结束在中间某个环节

一致性C:在事务开始之前和事务结束以后,数据库的完整性没有被破坏

隔离性I:事务的隔离性要求每个读写事务的对象与其他事务的操作对象能相互分离,即该事务提交前对其它事务都不可见

持久性D:事务一旦提交了,其结果就是永久的,就算发送了宕机等事故,数据库也能将数据恢复

##### 脏读是什么?

一个事务读取了另一个事务未提交的数据

##### 不可重复读是什么?

一个事务前后两次读取的同一数据不一致

##### 幻读是什么?

指一个事务两次查询的结果集记录数不一致

##### 什么是隔离性?

隔离性就是一个事务同另一个事务在并发情况下对一个数据进行修改时可以相互影响的一个程度

##### InnoDB的隔离级别你说说吧

| 隔离级别                 | 脏读 | 不可重复读 | 幻读 | 隔离性 | 并发性 |
| ------------------------ | ---- | ---------- | ---- | ------ | ------ |
| 顺序读                   | -    | -          | -    | 最高   | 最低   |
| 可重复读 repeatable read | -    | -          | -    |        |        |
| 读以提交                 | -    | +          | +    |        |        |
| 读未提交                 | +    | +          | +    | 最低   | 最高   |

##### 在MySQL中如何修改一个事务的隔离级别?

##### 什么时候需要使用事务管理机制？

对数据库的数据进行批量或连表操作时，为了保证数据的一致性和正确性，我们需要添加事务管理机制进行管理。当对数据库的数据进行操作失败时，事务管理可以很好保证所有的数据回滚到原来的数据，如果操作成功，则保证所有需要更新的数据持久化。 

##### 为什么会出现事务阻塞?

为了实现各个事务之间的隔离MySQL引入了锁的概念

1.查询需要对资源加共享锁S,被加锁的对象只能被持有锁的事务读取但并不能被修改,其他事务无法对该对象进行修改

2.数据修改需要对资源加排它锁x,被加排它锁的对象只能被持有锁的事务读取、修改,而其他事务无法读取、修改

|        | 排它锁 | 共享锁 |
| ------ | ------ | ------ |
| 排它锁 | 不兼容 | 不兼容 |
| 共享锁 | 不兼容 | 兼容   |

##### 如何检测阻塞?

```sql
select waiting_pid as '被阻塞的线程',

	waiting_query AS '被阻塞的SQL',

	blocking_pid AS '阻塞线程',

	wait_age AS'阻塞时间',

	sql_kill_blocking_query AS '建议操作'

from sys.innodb_lock_waits

--只有当阻塞时间超过30s才会被查询出来

where (UBIX_TIMESTAMP()-UNIX_TIMESTAMP(wait_started))>30
```

##### 如何处理事务中的阻塞?



##### 如何检测死锁?

在MySQL错误日志中记录死锁

```shell
set global innodb_print_all_deadlocks=on;
```

当MySQL一旦发送死锁我们就可以通过错误日志得到MySQL产生死锁的原因



##### 如何处理事务中的死锁?



##### 你之前工作中使用的是什么版本的Mysql?为什么选择这个版本?

反问面试官:当前贵公司使用的是什么版本的MySQL

我们使用的是PerconaMySQL他和官方MySQL版本是完全兼容的,并且其完全用于MySQL企业版才拥有的功能如:审计日志、防火墙、InnoDB热备份工具,而且其是在MySQL的基础上做个性能优化其性能还要优于官方企业版的MySQL

Mysql常见的发行版本:Percona MySQL、MariaDB

##### 各个版本之间的优缺点

服务器特性相比

| Mysql                      | PerconaMySQL      | MariaDB    |
| -------------------------- | ----------------- | ---------- |
| 开源                       | 开源              | 开源       |
| 支持分区表                 | 支持分区表        | 支持分区表 |
| InnoDB                     | XtraDB            | XtraDB     |
| 企业版监控工具社区版不提供 | PerconMonitor工具 | Monyog     |

高可用特性特性相比

| MySQL          | Perconal MySQL | MariaDB                          |
| -------------- | -------------- | -------------------------------- |
| 基于日志点复制 | 基于日志点复制 | 基于日志点复制                   |
| 基于Gtid复制   | 基于Gtid复制   | 基于Gtid复制,单Gitd同MySQL不兼容 |
| MGR            | MGR&PXC        | Galera Cluster                   |
| MySQL Router   | Proxy SQL      | MaxScale                         |

安全特性相比

| MySQL            | Percona MySQL     | MariaDB           |
| ---------------- | ----------------- | ----------------- |
| 企业版防火墙     | ProxySQL FireWall | MaxScale FireWall |
| 企业版用户审计   | 审计日志          | 审计日志          |
| 用户密码生命周期 | 用户密码生命周期  | -                 |

开发及管理特性相比

| MySQL           | Percona MySQL   | MariaDB              |
| --------------- | --------------- | -------------------- |
| 窗口函数(8.0)   | 窗口函数(8.0)   | 窗口函数(10.2)       |
| -               | -               | 支持基于日志回滚     |
|                 |                 | 支持记在表中记录修改 |
| Super read_only | Super read_only | -                    |

##### 如何决定是否对Mysql进行升级?如何进行升级?

升级可以给业务带来的益处

##### 在对MySQL进行升级前要考虑什么?

我们当前的业务数据库,主从延时十分的明显,有时可以达到几个小时的延迟,升级到高版本的MySQL之后那,大幅度降低主从延迟的时间5.6->5.7时,5.7增强了多线程复制,所以在应用re日志的时候,支持更好的并发性,从而减少延迟时间

Mysql5.7就升级了5.6的索引视图的性能和监控,又引入了sys这个视图方便DBA对Mysql进行监控

2.升级可以能对业务带来的影响

升级mysql数据库可能会对我们的业务

3.数据库升级方案的制定

4.升级失败的回滚方案

##### MySQL升级的步骤是什么?

1.对待升级数据库进行备份

2.升级Slave服务器版本

3.手动进行主从切换

4.升级MASTER服务器

5.升级完成后进行业务检查

##### 最新的Mysql版本是什么?他有什么特性比较吸引你?

| 新特性                                 |
| -------------------------------------- |
| 所有元数据使用InnoDB存储引擎,无frm文件 |
| 系统表采用InnoDB存储并采用独立表空间   |
|                                        |
|                                        |

在主从复制中,高版本的数据库是可以作为低版本数据库的从来使用的,而反过来会出问题

