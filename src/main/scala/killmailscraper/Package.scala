package killmailscraper

import com.typesafe.scalalogging._

object Package
  extends App
    with LazyLogging
    with SlickCodegen.Env {
  logger.info(s"Scrapping service is starting with queueId=${config.getConfig("killmailscraper").getString("queueID")} " +
    s"and ttw=${config.getConfig("killmailscraper").getInt("ttw")} ...")
  val scrape = new Scrape(db, config.getConfig("killmailscraper"))

  logger.info("Scraper service is started")
  scrape.run()
}