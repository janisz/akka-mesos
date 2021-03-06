package akka.mesos

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.libprocess._
import akka.mesos.MesosFrameworkActor.Deactivate
import akka.mesos.protos._
import akka.mesos.scheduler.SchedulerPublisher.SchedulerMessage
import akka.mesos.scheduler.{ SchedulerPublisher, SchedulerDriverActor, SchedulerDriver }
import akka.stream.scaladsl.Source
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.Try

object Mesos extends ExtensionId[MesosExtension] with ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension] = this

  override def createExtension(system: ExtendedActorSystem): MesosExtension =
    new MesosExtension(system)
}

class MesosExtension(system: ExtendedActorSystem) extends Extension {
  import system.dispatcher
  implicit val timeout: Timeout = system.settings.config.getDuration("akka.libprocess.timeout", TimeUnit.MILLISECONDS).millis

  def registerFramework(master: => Try[PID], framework: FrameworkInfo): Framework = {
    val driverRef = system.actorOf(Props(new SchedulerDriverActor(framework.name)))
    new Framework {
      private val frameworkActor = system.actorOf(Props(new MesosFrameworkActor(master, framework, driverRef)))

      override def shutdown() = frameworkActor ! PoisonPill
      override def deactivate() = frameworkActor ! Deactivate

      override val driver: SchedulerDriver = new SchedulerDriver(framework, frameworkActor, driverRef)

      override val schedulerMessages: Source[SchedulerMessage] = Source(Props(new SchedulerPublisher(frameworkActor)))
    }
  }
}

trait Framework {
  def schedulerMessages: Source[SchedulerMessage]
  def driver: SchedulerDriver
  def shutdown(): Unit
  def deactivate(): Unit
}
