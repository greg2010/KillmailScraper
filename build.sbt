name := "KillmailScraper"

version := "1.0"

scalaVersion := "2.12.1"


resolvers += Resolver.jcenterRepo
resolvers ++=
  Seq("twitter-repo" at "https://maven.twttr.com",
    "Wemesh Snapshots" at "http://maven.wemesh.ca/content/repositories/snapshots",
    "Wemesh Releases" at "http://maven.wemesh.ca/content/repositories/releases",
    "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
    Resolver.bintrayRepo("kwark", "maven"))

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.slick" %% "slick" % "3.2.0-M2",
  "io.spray" %% "spray-json" % "1.3.3",
  "org.scalaj" %% "scalaj-http" % "2.3.0")