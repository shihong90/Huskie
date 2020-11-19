package example.scala.actor
import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor._

/**
 * 相当一个server(服务端)
 */
class Echo extends Actor {
  def act() {
    alive(9010)
    register('myName, self)
    //(组合子)

    loop {
      react {
        case msg  =>  println(msg)
      }
    }
  }
}

object EchoServer  {

  def main(args: Array[String]): Unit = {
    val echo = new Echo
    echo.start
    println("Echo server started")
  }
}
