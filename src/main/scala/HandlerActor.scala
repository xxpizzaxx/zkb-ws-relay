import org.mashupbots.socko.events.WebSocketFrameEvent

import akka.actor.Actor
import akka.event.Logging

/**
 * Echo web socket frames for the Autobahn test suite
 */
class HandlerActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case event: WebSocketFrameEvent =>
      // Echo framed that was received
      event.context.writeAndFlush(event.wsFrame)
    case _ => {
      log.info("received unknown message of type: ")
    }
  }

}