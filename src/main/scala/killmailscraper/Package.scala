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
  val iterations = 1
  def recurse: Unit = {
    logger.debug(s"Dispatching request #")
    val result = scrape.getAndLogKillmail
    Thread.sleep(500)
    recurse
  }
  recurse
}