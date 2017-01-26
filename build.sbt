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

val slickVersion = "3.2.0-M2"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.github.tminglei" %% "slick-pg" % "0.15.0-M3",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "io.spray" %% "spray-json" % "1.3.3",
  "org.scalaj" %% "scalaj-http" % "2.3.0")


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