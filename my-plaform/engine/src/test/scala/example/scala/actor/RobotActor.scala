package example.scala.actor

import akka.actor.{Actor, ActorSystem, Props}


trait Action{
  val message:String
  val time:Int
}

case class TurnOnLight(time:Int) extends Action{
  val message = "小爱同学，把房间的等打开"
}

case class TurnOffLight(time:Int) extends Action{
  val message = "小爱同学，把房间的等关掉"
}

class RobotActor extends Actor{
  override def receive: Receive = {
    case t:TurnOnLight=>{
      println(s"${t.message} after ${t.time} hour")
    }
    case b:TurnOffLight=>{
      println(s"${b.message} after ${b.time} hour")
    }
    case _=>{
      println("不能处理消息")
    }
  }
}

object RobotActor extends App{
  val actorSystem = ActorSystem("xiaoAiStudent")
  val roboAcgtor = actorSystem.actorOf(Props(new RobotActor()),"robotActor")
  roboAcgtor ! TurnOnLight(1)
  roboAcgtor ! TurnOffLight(2)
  roboAcgtor ! "who are you "
}


