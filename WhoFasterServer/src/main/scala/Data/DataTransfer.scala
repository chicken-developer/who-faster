package Data

object DataTransfer {
  trait DataTransfer
  case class Position(x: Int, y: Int, z: Int) extends DataTransfer
  case class Rotation(pitch: Int, roll: Int, yaw: Int) extends DataTransfer
  case class Velocity(vx: Int, vy: Int, vz: Int) extends DataTransfer

  case class Motion(pos: Position, rot: Rotation, vel: Velocity) extends DataTransfer
  case class Player(name: String, data: Motion) extends DataTransfer
}
