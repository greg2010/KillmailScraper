package SlickCodegen

import slick.codegen._
import CustomPostgresDriver._
import org.red.killmailscraper.Env
import slick.jdbc.PostgresProfile

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await


object CustomCodeGen extends Env {
  def main(args: Array[String]): Unit = {
    Await.ready(
      codegen.map(_.writeToFile(
        "SlickCodegen.CustomPostgresDriver",
        args(0),
        "org.red.killmailscraper.db.models", // package under which the generated code is placed
        "Tables", // container
        "Tables.scala" // filename
      )),
      90 seconds
    )
  }
    private val codegen = db.run {

      val list = PostgresProfile.defaultTables

      list.flatMap { x =>
        println(x)
        PostgresProfile.createModelBuilder(x, true).buildModel
      }
    }.map { model => new SourceCodeGenerator(model) }

}