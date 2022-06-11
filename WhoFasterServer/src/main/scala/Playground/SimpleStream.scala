//package Playground
//
//import akka.NotUsed
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
//object SimpleStream extends App{
//
//  trait Cmd
//  case class IntCmd(num: Int, sender: ActorRef[Cmd]) extends Cmd
//  case class IntResult(num: Int) extends Cmd
//
//  case class Message(msg: IntCmd) extends Cmd
//  case object Complete extends Cmd
//  case class Fail(ex: Exception) extends Cmd
//
//
//  object FlowWorker {
//    def apply(): Behavior[Cmd] = Behaviors.receive { (context, message) =>
//      message match {
//        case IntCmd(num, destination) =>
//          context.log.info(s"[worker] Crunching data for $num")
//          destination ! IntResult(num * 2)
//          Behaviors.same
//        case _ => Behaviors.same
//      }
//    }
//  }
//
//
//  val numbersSource = Source(1 to 10)
//  val numberSink = Sink.foreach(println)
//  implicit val system = ActorSystem(Behaviors.empty, "System")
//  implicit val timeout = Timeout(2 seconds)
//
//  val actorTypedBasedFlow: Flow[Cmd, Cmd, NotUsed] = ActorFlow.ask(parallelism = 8)(ActorSystem(FlowWorker(), "FlowWorker"))(makeMessage = (cmd, sender) => )
//  numbersSource.viaMat(actorTypedBasedFlow)(Keep.both).to(numberSink).run()
//
//
//
//  val source: Source[Cmd, ActorRef[Cmd]] = ActorSource.actorRef[Cmd](completionMatcher = {
//    case Complete =>
//  }, failureMatcher = {
//    case Fail(ex) => ex
//  }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)
//
//
//  val ref = source
//    .collect {
//      case Message(intMsg) => intMsg
//    }.via(actorTypedBasedFlow)
//
//
//  ref ! Message("msg1")
//
//}
