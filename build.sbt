name := """play-authentication-mongo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  filters,
  javaWs,
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "font-awesome" % "4.3.0",
  "com.ning" % "async-http-client" % "1.8.14",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "jp.t2v" %% "play2-auth" % "0.13.2",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test")

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator
