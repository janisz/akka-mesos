package akka.mesos.protos.internal

import akka.mesos.protos.{ SlaveID, ExecutorID, FrameworkID }
import mesos.internal.Messages

final case class ExitedExecutorMessage(
    slaveId: SlaveID,
    frameworkId: FrameworkID,
    executorId: ExecutorID,
    status: Int) {
  def toProto: Messages.ExitedExecutorMessage =
    Messages.ExitedExecutorMessage
      .newBuilder
      .setSlaveId(slaveId.toProto)
      .setFrameworkId(frameworkId.toProto)
      .setExecutorId(executorId.toProto)
      .setStatus(status)
      .build()
}
