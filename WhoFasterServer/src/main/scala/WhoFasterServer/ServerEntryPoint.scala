package WhoFasterServer

import WhoFasterServer.Actors.GameAreaActor
import WhoFasterServer.Actors.GameAreaActor.GameAreaCommand
import WhoFasterServer.Config.SysConf
import WhoFasterServer.CustomFlow.GameAreaFlow
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

object ServerEntryPoint {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[GameAreaCommand] = ActorSystem(GameAreaActor(), "GameAreaActor")
    implicit val exctionContext = system.executionContext
    implicit val materializer: ActorMaterializer.type = ActorMaterializer


    val gameBind = Http().newServerAt(SysConf.localHost, SysConf.localPort).bindFlow(GameAreaFlow().finalFlow)

    println(s"Server is running on: http:// ${SysConf.localHost} :${SysConf.localPort}")
    println(s"Press ENTER or ANY KEY to stop")
    StdIn.readLine()
    gameBind.flatMap(_.unbind()).onComplete { _ =>
      system.terminate()
    }


  }

}
