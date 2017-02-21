package org.red.killmailscraper

import org.red.killmailscraper.RedisQ.RedisQSchema.KillPackage
import org.red.killmailscraper.db.DBController

class ScraperController {

  def run(): Unit = {
    val redisQApi = new RedisQ.ReqisQAPI(scraperConfig.getString("queueID"))

    def iterate(): Unit = {
      val response: KillPackage = redisQApi.poll()
      DBController.pushToDB(response)
      iterate()
    }

    iterate()
  }
}
