object Main extends App {
  val port = 9009
  val s = new Server(port)
  s.start()
}