package org.red.killmailscraper

import com.typesafe.scalalogging.LazyLogging
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze.SimpleHttp1Client
import org.red.killmailscraper.db.DBController
import org.red.zkb4s.RedisQ._
import org.red.zkb4s.RedisQ.RedisQSchema._
import spray.json.DeserializationException

import scala.annotation.tailrec
import scala.util.control.NonFatal
import scala.concurrent.duration._

class ScraperController extends LazyLogging {

  def run(): Unit = {
    val redisQApi = new ReqisQAPI(scraperConfig.getString("queueID"), scraperConfig.getString("ttw").toInt)
    implicit val c = SimpleHttp1Client()
    @tailrec def iterate(): Unit = {
      val response: KillPackage = redisQApi.poll()
      DBController.pushToDB(response)
      iterate()
    }

    try {
      iterate()
    } catch {
      case ex: UnexpectedStatus if ex.status.code == 429 => {
        val sleepTime: Int = scraperConfig.getInt("ttw") / 2
        logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
        Thread.sleep(sleepTime.seconds.toMillis)
        iterate()
      }
      case ex: DeserializationException => {
        logger.warn(s"Error deserializing json object, cause: ${ex.cause}" +
          s" fieldNames: ${ex.fieldNames} message: ${ex.msg}")
        iterate()
      }
      case ex if NonFatal(ex) => {
        logger.error(s"General run exception", ex)
        iterate()
      }
    }
  }
}