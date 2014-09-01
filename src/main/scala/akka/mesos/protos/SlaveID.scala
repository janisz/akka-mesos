package akka.mesos.protos

import org.apache.mesos.Protos.{ SlaveID => PBSlaveID }

final case class SlaveID(value: String) {
  def toProto: PBSlaveID =
    PBSlaveID
      .newBuilder
      .setValue(value)
      .build()
}
