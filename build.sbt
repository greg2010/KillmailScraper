name := "KillmailScraper"

version := "1.0"

scalaVersion := "2.12.1"

mainClass in (Compile, run) := Some("killmailscraper.Package")

resolvers += Resolver.jcenterRepo
resolvers ++=
  Seq("twitter-repo" at "https://maven.twttr.com",
    "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/",
    "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
    Resolver.bintrayRepo("kwark", "maven"))

val slickVersion = "3.2.0-RC1"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.github.tminglei" %% "slick-pg" % "0.15.0-M4",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "io.spray" %% "spray-json" % "1.3.3",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.8",
  "org.http4s" %% "http4s-core" % "0.15.3a",
  "org.http4s" %% "http4s-blaze-client" % "0.15.3a",
  "org.http4s" %% "http4s-argonaut" % "0.15.3a",
  "org.http4s" %% "http4s-dsl" % "0.15.3a",
  "org.http4s" %% "http4s-client" % "0.15.3a"
)


slick <<= slickCodeGenTask // register manual sbt command
//sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use


// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceDirectory, fullClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "main/scala").getPath
  val fileName = outputDir + "/killmailscraper/db/models/Tables.scala"
  toError(r.run("SlickCodegen.CustomCodeGen", cp.files, Array(outputDir), s.log))
  Seq(file(fileName))
}