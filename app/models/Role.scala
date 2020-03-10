package models

sealed trait Role

object Role {
  case object Admin extends Role
  case object Normal extends Role

  def valueOf(value: String): Role = value match {
    case "Administrator" => Admin
    case "NormalUser"    => Normal
    case _               => throw new IllegalArgumentException
  }

  def stringValueOf(value: Role): String = value match {
    case Admin  => "Administrator"
    case Normal => "NormalUser"
    case _      => throw new IllegalArgumentException
  }
}
