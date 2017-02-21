package org.red.killmailscraper

import com.typesafe.scalalogging._

object Server
  extends App
    with LazyLogging
    with Env {
  logger.info(s"Scrapping service is starting with queueId=${config.getConfig("killmailscraper").getString("queueID")} " +
    s"and ttw=${config.getConfig("killmailscraper").getInt("ttw")} ...")
  val scrape = new Scrape(db, config.getConfig("killmailscraper"))

  logger.info("Scraper service is started")
  scrape.run()
}