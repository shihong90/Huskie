package example.scala.actor

import scala.actors.Actor

/**
 * 总结:
 * 1.最基本的字符串消息发送与接受
 * 2.发送case 类类型的消息
 * 3.actor与actor之间的通讯如何实现
 *
 * 作业:
 * 远程actor(remote actor)
 * ip
 * 端口
 *
 * @param msg
 * @param targetActor
 */
case class SayHello(msg: String, targetActor: Actor)

class HiActor extends Actor {
  override def act(): Unit = {
    while (true) {
      receive {
        case "Hi" => println("Hello")
        case "SayHello" => println("HiActor say hello")
        case SayHello(msg, targetActor) => {
          println("msg:" + msg)
          targetActor ! "Hello"
        }
        case _ => println("无法处理消息")
      }
    }
  }
}

class SayHelloActor extends Actor {
  override def act(): Unit = {
    while (true) {
      receive {
        case "Hello" => println("SayHelloActor Say Hello")
        case "hihi" => println("SayHelloActor hihi")
        case _ => println("无法处理消息")
      }
    }
  }
}

object HiActor {
  def main(args: Array[String]): Unit = {
    val sayHelloActor = new SayHelloActor
    sayHelloActor.start()

    val hiActor = new HiActor
    hiActor.start()
    hiActor ! "SayHello"
    hiActor ! SayHello("hihi",sayHelloActor)
  }
}
