package WhoFaster_vBeta

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
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
    val gameBind = Http().newServerAt(localHost, localPort).bindFlow(???)

    println(s"Server is running on: http://$localHost:$localPort")
    StdIn.readLine()
    gameBind.flatMap(_.unbind()).onComplete { _ =>
      system.terminate()
    }
  }
  //End
}
