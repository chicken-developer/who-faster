package Playground

package Playground
import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object GameServer {
  /*
  Source: PlayerAction(Position:1,1,2 ; xInc)
  Flow: Handle and return new position: PlayerAction(Position: 2,1,2, idle)
  Sink (PlayerAction(Position: 2,1,2)
   */

  trait Protocol

  case class Message(msg: PlayerData) extends Protocol
  case class MessageWithSender(msg: Message, sender: ActorRef[Protocol]) extends PlayerData
  case class MessageWithSenderAndAck(ackTo: ActorRef[Ack], msgFromFlow: Protocol) extends Protocol

  case object Complete extends Protocol
  case class Fail(ex: Exception) extends Protocol

  trait Ack
  object Ack extends Ack
  case class Init(ackTo: ActorRef[Ack]) extends Protocol
  case class FailAndThrowEx(ex: Throwable) extends Protocol

  trait PlayerAction extends Protocol
  case class xIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
  case class yIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
  case class zIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
  case class idleReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction

  trait PlayerData extends Protocol
  case class Position(xPos: Int, yPos: Int, zPos: Int) extends PlayerData
  case class Player(name: String, pos: Position, action: PlayerAction) extends PlayerData

  implicit val systemRef: ActorSystem[Protocol] = ActorSystem(
    Behaviors.receive[Protocol] { (context, message) =>
      message match {
        case _ => context.log.info(s"[System] Receive: $message")
      }
      Behaviors.same
    }, "Ghost")

  implicit val timeout: Timeout = Timeout(2 seconds)

  object GraphComp {

    object PlayerDataFlow {
      def apply(): Behavior[Protocol] = Behaviors.receive { (context, message) =>
        message match {
          case MessageWithSender(msg, sender) =>
            context.log.info(s"Receive from flow: $message")
            sender ! MessageWithSender(msg, sender)
            Behaviors.same
          case _ =>
            context.log.info(s"Receive a message can't handle: $message")
            Behaviors.same
        }
      }
    }

    object SinkActorHandler {
      def apply(): Behavior[Protocol] = Behaviors.receive { (context, message) =>
        message match {
          case Init(responseActorRef) =>
            context.log.info("Stream initialized")
            responseActorRef ! Ack
            Behaviors.same
          case Complete =>
            context.log.info("Stream complete")
            Behaviors.same
          case FailAndThrowEx(exception) =>
            context.log.error(s"Stream failed: $exception")
            Behaviors.same
          case MessageWithSenderAndAck(responseActorRef, element) =>
            context.log.info(s"Here final out message: $element")
            responseActorRef ! Ack
            Behaviors.same
        }
      }
    }

    val playerSource: Source[Protocol, ActorRef[Protocol]] = ActorSource.actorRef[Protocol](completionMatcher = {
      case Complete =>
    }, failureMatcher = {
      case Fail(ex) => ex
    }, bufferSize = 10, overflowStrategy = OverflowStrategy.dropHead)

    val actorTypedBasedFlow: Flow[Message, Protocol, NotUsed] = ActorFlow.ask(parallelism = 8)(ActorSystem(PlayerDataFlow(), "PlayerDataFlowActor"))(makeMessage = (msg, sender) => MessageWithSender(msg, sender))

    val superSink: Sink[Protocol, NotUsed] = ActorSink.actorRefWithBackpressure(
      ref = ActorSystem(SinkActorHandler(), "SuperSink"),
      messageAdapter = (responseActorRef: ActorRef[Ack], element) => MessageWithSenderAndAck(responseActorRef, element),
      onInitMessage = (responseActorRef: ActorRef[Ack]) => Init(responseActorRef),
      ackMessage = Ack,
      onCompleteMessage = Complete,
      onFailureMessage = (exception) => FailAndThrowEx(exception))

    val finalGraph: ActorRef[Protocol] = playerSource
      .collect {
        case Message(msg) => Message(msg)
      }.via(actorTypedBasedFlow).to(superSink).run()
  }

  def main(args: Array[String]): Unit = {
    import GraphComp._
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))
    finalGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq()))

  }


}

