package com.kkb.utils

import com.kkb.domain.engine.PlatEngine
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.{ZkMarshallingError, ZkNoNodeException, ZkNodeExistsException}
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.zookeeper.data.Stat

import scala.collection.Seq

object ZKUtils {
  //我们要使用zk，就需要获取zk的客户端
  var zkClient: ZkClient = null
  //session会话的超时时间
  val sessionTimeout = 60000
  //连接超时
  val connectionTimeout = 60000

  //引擎路径(zookeeper中的路径),路径就是节点(持久化的节点，一直存在)
  var engine_path = "/platform/engine"

  //actor的引擎路径
  var valid_engine_path = "/platform/valid_engine"

  def getZkClient(zkServers: String): ZkClient = {
    if (zkClient == null) {
      zkClient = new ZkClient(zkServers, sessionTimeout, connectionTimeout, new ZkSerializer {
        override def serialize(o: Any): Array[Byte] = {
          try {
            //对zk中存储的数据进行序列化，并且设置编码UTF-8
            o.toString.getBytes("UTF-8")
          } catch {
            case e: ZkMarshallingError => return null
          }
        }

        override def deserialize(bytes: Array[Byte]): AnyRef = {
          try {
            new String(bytes, "UTF-8")
          } catch {
            case e: ZkMarshallingError => return null
          }
        }
      })
    }
    zkClient
  }

  def createPersistentPathIfNotExists(zkClient: ZkClient, path: String): Unit = {
    if (!zkClient.exists(path)) {
      //true，父目录不存在的时候优先创建父目录，在创建子目录
      zkClient.createPersistent(path, true);
    }
  }

  //创建父目录
  def createParentPath(zkClient: ZkClient,path:String):Unit = {
    val parentDir  = path.substring(0,path.lastIndexOf("/"))
    if(parentDir.length!=0){
      //父目录创建的是持久化的节点（节点路径在zk中长久存在）
      zkClient.createPersistent(parentDir,true)
    }
  }

  def createEphemeralPathAndParentPathIfNotExists(zkClient: ZkClient, path: String, data: String): Unit = {
    zkClient.createEphemeral(path, data)
  }

  /**
   * 向zk中注册引擎
   * @param zkClient
   * @param id
   * @param host
   * @param port
   */
  def registerEngineInZookeeper(zkClient: ZkClient,id:Int,host:String,port:Int):Unit = {
    //规划引擎的路径
    val brokerIdPath = ZKUtils.engine_path+s"/${id}"
    //brokerInfo:ip:端口  引擎信息
    val brokerInfo =s"${host}:${port}"
    try {
      createPersistentPathIfNotExists(zkClient, ZKUtils.engine_path)
      createEphemeralPathAndParentPathIfNotExists(zkClient, brokerIdPath, brokerInfo)
    }catch{
      case e:ZkNodeExistsException=>{
        throw new RuntimeException("注册失败，节点已经存在!")
      }
    }
  }

  /**获取PlatEngine信息*/
  def getPlatEngine(zkClient: ZkClient , engineId:Int):Option[PlatEngine] = {
    //指定节点路径（engineId），获取不同的节点信息（不同引擎信息）
    val dataAndStat: (Option[String], Stat) = ZKUtils.readDataMaybeNotExist(zkClient , ZKUtils.engine_path+s"/${engineId}")
    //将获取到的信息封装成引擎信息
    dataAndStat._1 match {
        //engineInfo:ip:端口
      case Some(engineInfo) => Some(PlatEngine(engineId, engineInfo))
      case None => None
    }
  }

  /**读取路径下的数据*/
  def readDataMaybeNotExist(zkClient: ZkClient , path:String):(Option[String] , Stat) = {
    //构建一个stat，里面封装了zk节点信息
    val stat = new Stat()
    //读取信息，返回（ip:port , 节点信息）
    val dataAndSate = try{
      (Some(zkClient.readData(path , stat)) , stat)
    }catch {
      case e1:ZkNoNodeException =>(None , stat)
      case e2:Throwable => throw e2
    }
    dataAndSate
  }

  /**获取子节点*/
  def getChildrenMayNotExist(client: ZkClient, path: String): Seq[String] = {
    import scala.collection.JavaConversions._
    try {
      client.getChildren(path)
    } catch {
      case e: ZkNoNodeException => return Nil
      case e2: Throwable => throw e2
    }
  }

  /**从集群多台机器获取PlatEngine*/
  def getPlatEngineInCluster(zkClient: ZkClient):Seq[PlatEngine] = {
    //得到子节点 ， 子节点用id代表（数字）
    val childSortedData: Seq[String] = ZKUtils.getChildrenMayNotExist(zkClient , engine_path).sorted
    //返回的是string，类型转换一下
    val childSortedData2Int: Seq[Int] = childSortedData.map(line => line.toInt)
    //根据节点名称，获取节点里面的信息，最后封装成引擎信息
    val engineDatas: Seq[Option[PlatEngine]] = childSortedData2Int.map(line => ZKUtils.getPlatEngine(zkClient , line))
    //得到具体的引擎集合
    val platEngines: Seq[PlatEngine] = engineDatas.filter(_.isDefined).map(line => line.get)
    platEngines
  }

  /**注册jobActor引擎*/
  def registerActorInPlatEngine(zkClient: ZkClient, path: String,data: String): Unit ={
    try{
      //如果父路径不存在，那么先创建父路径
      createPersistentPathIfNotExists(zkClient , ZKUtils.valid_engine_path)
      createEphemeralPathAndParentPathIfNotExists(zkClient ,path , data)
    }catch {
      case e: ZkNodeExistsException =>
        throw new RuntimeException("注册jobActor引擎出现了异常")
    }
  }
}
