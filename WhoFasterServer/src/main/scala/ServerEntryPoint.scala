import Actors.GameAreaActor
import Actors.GameAreaActor.GameAreaCommand
import Config.SysConf
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.io.StdIn

object ServerEntryPoint {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[GameAreaCommand] = ActorSystem(GameAreaActor(), "GameAreaActor")
    implicit val exctionContext = system.executionContext
    implicit val materializer: ActorMaterializer.type = ActorMaterializer
    import system.dispatchers


    val gameBind = Http().newServerAt(SysConf.localHost, SysConf.localPort).bindFlow(finalFlow)

    println(s"Server is running on: http:// ${SysConf.localHost} :${SysConf.localPort}")
    println(s"Press ENTER or ANY KEY to stop")
    StdIn.readLine()
    gameBind.flatMap(_.unbind()).onComplete { _ =>
      system.terminate()
    }


  }

}