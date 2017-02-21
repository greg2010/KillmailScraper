name := "KillmailScraper"

version := "1.0"

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
    Resolver.bintrayRepo("kwark", "maven"))

val slickVersion = "3.2.0-RC1"
val http4sVersion = "0.15.5a"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.github.tminglei" %% "slick-pg" % "0.15.0-M4",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "io.spray" %%  "spray-json" % "1.3.3",
  "org.http4s" %% "http4s-core" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion
)


slick <<= slickCodeGenTask // register manual sbt command
//sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use


// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceDirectory, fullClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "main/scala").getPath
  val fileName = outputDir + "/org/red/killmailscraper/db/models/Tables.scala"
  toError(r.run("SlickCodegen.CustomCodeGen", cp.files, Array(outputDir), s.log))
  Seq(file(fileName))
}