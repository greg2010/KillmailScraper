package killmailscraper

import com.typesafe.scalalogging._

object Package
  extends App
    with LazyLogging
    with SlickCodegen.Env {
  logger.info("Scrapping service is starting...")
  val scrape = new Scrape(db, config.getConfig("killmailscraper"))

  logger.info("Scraper service is started")
  scrape.run()
}