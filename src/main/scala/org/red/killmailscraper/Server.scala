package org.red.killmailscraper

import com.typesafe.scalalogging._

object Server
  extends App
    with LazyLogging {
  logger.info(s"Scrapping service is starting with queueId=${scraperConfig.getString("queueID")} " +
    s"and ttw=${scraperConfig.getInt("ttw")} ...")
  val scraperController = new ScraperController
  logger.info("Scraper service is started")
  scraperController.run()
}