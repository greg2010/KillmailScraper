package killmailscraper

import com.typesafe.scalalogging._

import org.http4s.client._

object Package
  extends App
    with LazyLogging
    with SlickCodegen.Env{
  val scrape = new Scrape(db)
  /*def recurse(iterationCount: Long): Unit = {
    logger.info(s"Dispatching request #${iterationCount}")
    val result = scrape.getAndLogKillmail
    //Thread.sleep(config.getString("killmailscraper.timeBetweenRequests").toInt)
    recurse(iterationCount + 1)
  }
  recurse(0)*/
  scrape.run()
}