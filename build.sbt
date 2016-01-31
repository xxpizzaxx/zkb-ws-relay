name := "zkb-ws-relay"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.jcenterRepo

libraryDependencies += "org.mashupbots.socko" %% "socko-webserver" % "0.6.0"
libraryDependencies += "moe.pizza" %% "eveapi" % "0.32" exclude("org.slf4j", "slf4j-simple")
