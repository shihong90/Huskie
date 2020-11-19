package example.scala.actor

import scala.actors.Actor
//同步消息
case class SynMsg(id:Int,msg:String)
//异步消息
case class AsyncMsg(id:Int,msg:String)
//应答
case class ReplyMsg(id:Int,msg:String)

class AppleActor extends Actor{
  override def act(): Unit = {
    while(true){
      receive{
        case "start" =>{
          println("starting...")
        }
        case SynMsg(id,msg)=>{
          println(id+",sync msg:"+msg)
          Thread.sleep(5000)
          this.sender ! ReplyMsg(3,"finished")
        }
        case AsyncMsg(id,msg)=>{
          println(id+",async:"+msg)
          Thread.sleep(5000)
        }
        case _=>{
          println("无效的消息，无法处理")
        }
      }
    }
  }
}

object AppleActor{
  def main(args: Array[String]): Unit = {
    val a = new AppleActor
    a.start()
    //异步消息发送
    a ! AsyncMsg(1,"hello actor")

    val reply = a !! SynMsg(2,"hello actor")

    println(reply.apply())

  }
}
