package WhoFasterServer.Actors

import WhoFasterServer.Data.DataTransfer.{Motion, Player}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors


object GameAreaActor {
  trait GameAreaCommand

  trait IdleCommand extends GameAreaCommand// When in main menu
  case class EnterGame(playerName: String) extends IdleCommand
  case class ExitGame(playerName: String) extends IdleCommand
  case class EnterLobby(playerName: String, lobbyKey: String) extends IdleCommand
  case class ExitLobby(playerName: String) extends IdleCommand
  case class MainMenuUpdate(players: Map[String, ActorRef[GameAreaCommand]])

  trait InGameCommand extends GameAreaCommand
  case class JoinMatch(playerName: String) extends InGameCommand
  case class LeftMatch(playerName: String) extends InGameCommand
  case class MotionUpdate(playerName: String, newMotion: Motion) extends InGameCommand


  def apply(): Behavior[GameAreaCommand] = IdleBehaviors

  def IdleBehaviors: Behavior[GameAreaCommand] = Behaviors.receive {(context, message) =>
    message match {
      case EnterGame(playerName) =>
        context.log.info(s"Player $playerName enter game")
        InGameBehaviors
    }
  }

  def InGameBehaviors: Behavior[GameAreaCommand] = Behaviors.receive { (context, message) =>
    message match {
      case JoinMatch(playerName) =>
        context.log.info("Player left match")
    }
    Behaviors.same
  }
}
