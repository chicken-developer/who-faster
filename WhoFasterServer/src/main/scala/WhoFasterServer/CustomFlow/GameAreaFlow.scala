package WhoFasterServer.CustomFlow

import WhoFasterServer.Actors.GameAreaActor
import WhoFasterServer.Actors.GameAreaActor.GameAreaCommand
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}

case class GameAreaFlow()(implicit val system: ActorSystem[GameAreaCommand], implicit val materializer: Materializer) {
  import GameAreaActor._

  val gameAreaActor: ActorSystem[GameAreaCommand] = ActorSystem(GameAreaActor(), "GameAreaActor")
  val gameAreaSource: Source[GameAreaCommand, ActorRef[GameAreaCommand]] = ???

//  def gameAreaFlow(players: Map[String, ActorRef[GameAreaCommand]]): Flow[Message, Message,Any] =
//    Flow.fromGraph {
//      GraphDSL.createGraph(gameAreaSource) { implicit builder => profileShape =>
//        import GraphDSL.Implicits._
//
//       ///
//      }
//    }

  def websocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
    case tm: TextMessage =>
      TextMessage(Source.single("Server says back:") ++ tm.textStream ++ Source.single("!"))
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      TextMessage(Source.single("Server received a binary message..."))
  }

  def finalFlow: Route = {
    (get & parameter("keyString")){ keyString =>
      //Verify key string
      println(s"Receive key: $keyString")
      handleWebSocketMessages(websocketFlow)
    }
  }
}
