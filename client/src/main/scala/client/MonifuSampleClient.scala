package client

import shared.models.Signal

import scala.scalajs.js

object MonifuSampleClient extends js.JSApp {
  def main(): Unit = {
    println(Signal("event"))
  }
}
