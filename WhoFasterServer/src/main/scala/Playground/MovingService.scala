//package Playground
//import akka.NotUsed
//import akka.actor.{Actor, ActorLogging, Props}
//import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
//import akka.actor.typed.scaladsl.Behaviors
//import akka.stream.OverflowStrategy
//import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
//import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
//import akka.util.Timeout
//
//import scala.concurrent.duration.DurationInt
//import scala.language.postfixOps
//
//object MovingService extends App{
//  /*
//  Source: PlayerAction(Position:1,1,2 ; xInc)
//  Flow: Handle and return new position: PlayerAction(Position: 2,1,2, idle)
//  Sink (PlayerAction(Position: 2,1,2)
//   */
//
//  trait Protocol
//  case class Message(msg: PlayerData) extends Protocol
//  case class MessageWithSender(msg: Message, sender: ActorRef[Protocol]) extends PlayerData
//  case class MessageWithSenderAndAck(ackTo: ActorRef[Ack], msgFromFlow: Protocol) extends Protocol
//
//  case object Complete extends Protocol
//  case class Fail(ex: Exception) extends Protocol
//
//  trait Ack
//  object Ack extends Ack
//  case class Init(ackTo: ActorRef[Ack]) extends Protocol
//  case class FailAndThrowEx(ex: Throwable) extends Protocol
//
//  trait PlayerAction extends Protocol
//  case class xIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
//  case class yIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
//  case class zIncReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
//  case class idleReq()(implicit val sender: ActorRef[Protocol]) extends PlayerAction
//
//  trait PlayerData extends Protocol
//  case class Position(xPos: Int, yPos: Int, zPos: Int) extends PlayerData
//  case class Player(name: String, pos: Position, action: PlayerAction,implicit val sender: ActorRef[Protocol]) extends PlayerData
//
//
//
//  implicit val systemRef: ActorSystem[Protocol] = ActorSystem(
//   Behaviors.receive[Protocol] { (context, message) =>
//     message match {
//       case _ => context.log.info(s"[System] Receive: $message")
//     }
//      Behaviors.same
//  }, "Ghost")
//
//  val players = List(
//    Player("Pyke", Position(0,0,0), xIncReq(), systemRef),
//    Player("Ashe", Position(1,2,1), yIncReq(), systemRef),
//    Player("Nami", Position(4,2,4), zIncReq(), systemRef),
//    Player("Garen", Position(6,1,0), idleReq(), systemRef),
//  )
//
//  val simpleSource = Source(players)
//
//  object PlayerDataFlow {
//    def apply(): Behavior[Protocol] = Behaviors.receive { (context, message) =>
//      message match {
//        case MessageWithSender(msg, sender) =>
//          context.log.info("Enter flow and not handle anything")
//          context.log.info(s"Receive 1: $message")
//
//          sender ! MessageWithSender(msg, sender)
//          Behaviors.same
//        case _ =>
//          context.log.info(s"Receive 2: $message")
//          Behaviors.same
//      }
//    }
//  }
//
//  implicit val timeout = Timeout(2 seconds)
//  val actorTypedBasedFlow =  ActorFlow.ask(parallelism = 8)(ActorSystem(PlayerDataFlow(), "PlayerDataFlowActor"))(makeMessage = (msg, sender) => MessageWithSender(msg, sender) )
//
//  val simpleSink = Sink.foreach(println)
//
// // simpleSource.via(actorTypedBasedFlow).to(simpleSink)//.run()
//
//
//  val playerSource: Source[Protocol, ActorRef[Protocol]] = ActorSource.actorRef[Protocol](completionMatcher =  {
//    case Complete =>
//  }, failureMatcher = {
//    case Fail(ex) => ex
//  }, bufferSize = 10, overflowStrategy = OverflowStrategy.dropHead)
//
////  val actorAsGraph = playerSource
////    .collect {
////      case MessageInFlow(msg, sender) => MessageInFlow(msg, sender)
////  }.via(actorTypedBasedFlow).to(simpleSink).run()
//
////  actorAsGraph ! MessageInFlow(Player("Pyke", Position(0,0,0), xIncReq(), systemRef), systemRef)
////  actorAsGraph ! MessageInFlow(Player("Pyke", Position(0,0,0), xIncReq(), systemRef), systemRef)
////
////  actorAsGraph ! MessageInFlow(Player("Pyke", Position(0,0,0), xIncReq(), systemRef), systemRef)
////  actorAsGraph ! MessageInFlow(Player("Pyke", Position(0,0,0), xIncReq(), systemRef), systemRef)
////  actorAsGraph ! MessageInFlow(Player("Pyke", Position(0,0,0), xIncReq(), systemRef), systemRef)
//
///*
//case object StreamInit
//  case object StreamAck
//  case object StreamComplete
//  case class StreamFail(ex: Throwable)
//
//  class DestinationActor extends Actor with ActorLogging {
//    override def receive: Receive = {
//      case StreamInit =>
//        log.info("Stream initialized")
//        sender() ! StreamAck
//      case StreamComplete =>
//        log.info("Stream complete")
//        context.stop(self)
//      case StreamFail(ex) =>
//        log.warning(s"Stream failed: $ex")
//      case message =>
//        log.info(s"Message $message has come to its final resting point.")
//        sender() ! StreamAck
//    }
//  }
//  val destinationActor = system.actorOf(Props[DestinationActor], "destinationActor")
//
//  val actorPoweredSink = Sink.actorRefWithAck[Int](
//    destinationActor,
//    onInitMessage = StreamInit,
//    onCompleteMessage = StreamComplete,
//    ackMessage = StreamAck,
//    onFailureMessage = throwable => StreamFail(throwable) // optional
//  )
// */
///*
//trait Ack
//  object Ack extends Ack
//  case class Init(ackTo: ActorRef[Ack]) extends Protocol
//  case class FailAndThrowEx(ex: Throwable) extends Protocol
// */
//  object SinkActorHandler {
//    def apply(): Behavior[Protocol] = Behaviors.receive { (context, message) =>
//        message match {
//          case Init(responseActorRef) =>
//            context.log.info("Stream initialized")
//            responseActorRef ! Ack
//            Behaviors.same
//          case Complete =>
//            context.log.info("Stream complete")
//            Behaviors.same
//          case FailAndThrowEx(exception) =>
//            context.log.error(s"Stream failed: $exception")
//            Behaviors.same
//          case MessageWithSenderAndAck(responseActorRef, element) =>
//            context.log.info(s"AAAAAAAAAAAAAAAAAA")
//            responseActorRef ! Ack
//            Behaviors.same
//        }
//    }
//  }
//
//
//  val superSink = ActorSink.actorRefWithBackpressure(
//    ref = ActorSystem(SinkActorHandler(), "SuperSInk"),
//    messageAdapter = (responseActorRef: ActorRef[Ack], element) => MessageWithSenderAndAck(responseActorRef, element),
//    onInitMessage = (responseActorRef: ActorRef[Ack]) => Init(responseActorRef),
//    ackMessage = Ack,
//    onCompleteMessage = Complete,
//    onFailureMessage = (exception) => FailAndThrowEx(exception))
//
//  val actorAsGraph = playerSource
//    .collect {
//      case Message(msg) => Message(msg)
//    }.via(actorTypedBasedFlow).to(superSink).run()
//
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//  actorAsGraph ! Message(Player("Pyke", Position(0,0,0), xIncReq(), systemRef))
//
//}
