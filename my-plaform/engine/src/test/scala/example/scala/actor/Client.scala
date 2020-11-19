package example.scala.actor

import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._

object Client extends App {
  override def main(args: Array[String]) {
    if (args.length < 1) {
      println("Usage: scala Client [msg]")
      return
    }

    actor {
      val remoteActor = select(Node("localhost", 9010), 'myName)
      remoteActor !? args(0) match {
        case msg => println( "Server's response is [" + msg + "]" )
      }
    }
  }
}
