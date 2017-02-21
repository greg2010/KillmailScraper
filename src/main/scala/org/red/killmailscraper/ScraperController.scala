package org.red.killmailscraper

import org.red.killmailscraper.RedisQ.RedisQSchema.KillPackage
import org.red.killmailscraper.db.DBController

/**
  * Created by greg2010 on 21/02/17.
  */
class ScraperController {

  def run(): Unit = {
    def iterate(): Unit = {
      val response: KillPackage = RedisQ.ReqisQAPI.poll()
      DBController.pushToDB(response)
      iterate()
    }
    iterate()
  }
}
