package WhoFaster_vBeta

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.io.StdIn

object Backend {

  object Core {
    trait Protocol

  }

  object Controller {
    import Core._
    object GameAreaActor {
      def apply(): Behavior[Protocol] = Behaviors.receive { (context, message) =>
        Behaviors.same
      }
    }
  }
}

object ServerDraft {

  object Config {
    val localHost = "localhost"
    val localPort = 3000
  }

  def main(args: Array[String]): Unit = {
    import Backend.Core._
    import Backend.Controller._

    implicit val system: ActorSystem[Protocol] = ActorSystem(GameAreaActor(), "GameAreaActor")
    implicit val excutionContext = system.executionContext
    import Config._

    def websocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
      case tm: TextMessage =>
        TextMessage(Source.single("Server says back:") ++ tm.textStream ++ Source.single("!"))
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        TextMessage(Source.single("Server received a binary message..."))
    }

    def finalFlow: Route = {
      (get & parameter("c")){ keyString =>
        //Verify key string
        println(s"Receive key: $keyString")
        handleWebSocketMessages(websocketFlow)
      }
    }

    val gameBind = Http().newServerAt(localHost, localPort).bindFlow(finalFlow)

    println(s"Server is running on: http://$localHost:$localPort")
    StdIn.readLine()
    gameBind.flatMap(_.unbind()).onComplete { _ =>
      system.terminate()
    }
  }
  //End
}
