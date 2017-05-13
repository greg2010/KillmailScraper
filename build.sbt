name := "KillmailScraper"

version := "1.5.0"

scalaVersion := "2.12.1"

assemblyJarName in assembly := "killmailscraper.jar"
mainClass in assembly := Some("org.red.killmailscraper.Server")
// Hax to get .jar to execute
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList(_*) => MergeStrategy.first
}


mainClass in (Compile, run) := Some("org.red.killmailscraper.Server")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.jcenterRepo
resolvers ++=
  Seq("twitter-repo" at "https://maven.twttr.com",
    "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/",
    "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
    "Artifactory Realm" at "http://maven.red.greg2010.me/artifactory/sbt-local/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")


libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.red" %% "zkb4s" % "1.0.5",
  "org.red" %% "reddb" % "1.0.2-SNAPSHOT"
)
