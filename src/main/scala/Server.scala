import akka.actor.{ActorSystem, Props}
import moe.pizza.zkapi.ZKBAPI
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}
import scala.util.Try
import org.http4s._
import org.http4s.client.blaze._

class Server(port: Int) {
  val actorSystem = ActorSystem("zkb-relay-actor-system")
  val handler = actorSystem.actorOf(Props[HandlerActor])
  implicit val client = PooledHttp1Client()

  val routes = Routes({

    case WebSocketHandshake(wsHandshake) =>
        wsHandshake.authorize(maxFrameSize = Integer.MAX_VALUE)
    case WebSocketFrame(wsFrame) => {
      handler ! wsFrame
    }
  })

  val webServer = new WebServer(WebServerConfig(port = port), routes, actorSystem)

  val zkb = new ZKBAPI(useragent = "zkb-ws-relay", strict = false)

  val broadcaster = new Thread(new Runnable {
    override def run(): Unit = {
      import actorSystem.dispatcher
      while (true) {
          zkb.redisq.stream().foreach { r =>
              webServer.webSocketConnections.writeText(r.toString)
          }
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
