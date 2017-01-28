package killmailscraper

import com.typesafe.scalalogging._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success


object Package
  extends App
    with LazyLogging
    with SlickCodegen.Env{
  val scrape = new Scrape(db)
  val iterations = 100
  def recurse(count: Int): Unit = {
    if (count == 0) Unit
    else {
      logger.info(s"Dispatching request # ${100-count}")
      val result = scrape.getDeserializedJson
      recurse(count - 1)
      Await.result(result, Duration(100, "s"))
    }
  }
  recurse(iterations)
}