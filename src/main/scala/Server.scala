import akka.actor.{ActorSystem, Props}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import moe.pizza.zkapi.ZKBAPI
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}

class Server(port: Int) {
  val actorSystem = ActorSystem("zkb-relay-actor-system")
  val handler = actorSystem.actorOf(Props[HandlerActor])

  val routes = Routes({

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/") => {
        wsHandshake.authorize(maxFrameSize = Integer.MAX_VALUE)
      }
    }
    case WebSocketFrame(wsFrame) => {
      handler ! wsFrame
    }
  })

  val webServer = new WebServer(WebServerConfig(port = port), routes, actorSystem)

  val zkb = new ZKBAPI(useragent = "zkb-ws-relay", strict = false)
  val OM = new ObjectMapper()
  OM.registerModule(DefaultScalaModule)

  val broadcaster = new Thread(new Runnable {
    override def run(): Unit = {
      import actorSystem.dispatcher
      zkb.redisq.stream().foreach { r =>
        webServer.webSocketConnections.writeText(OM.writeValueAsString(r))
      }
    }
  })

  def start() = {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run {
        webServer.stop()
        broadcaster.stop()
      }

    })
    webServer.start()
    broadcaster.start()

    System.out.println("Server started on ws://localhost:%d".format(port))
  }

}
