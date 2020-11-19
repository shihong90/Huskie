package example.scala.actor

import scala.actors.Actor

class HelloActor extends Actor{
  //需要一个线程启动它，act类似java线程中的run方法，功能完全一样
  override def act(): Unit = {
    /**
     * aa match{
     *  case
     *  case
     * }
     *
     * fn:():unit =>{}
     *
     * fn{}
     *
     *
     *
     *
     */
    while(true){
      receive{
        case name:String => println("Hello,"+name)
        case _=>println("无法处理消息")
      }
    }
  }
}
object HelloActor{
  def main(args: Array[String]): Unit = {
    //1.创建actor的实例
    val helloActor = new HelloActor
    //2.启动actor实例,start()类似java中启动一个线程的start（）方法
    helloActor.start()
    helloActor ! "tom"
  }
}
