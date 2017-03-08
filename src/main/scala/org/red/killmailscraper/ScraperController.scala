package org.red.killmailscraper

import javax.net.ssl.SSLContext

import com.typesafe.scalalogging.LazyLogging
import io.circe.ParsingFailure
import org.red.zkb4s.RedisQ._

import scala.util.control.NonFatal
import scala.concurrent.duration._

class ScraperController extends LazyLogging {

  def run(): Unit = {
    val redisQApi = new ReqisQAPI(scraperConfig.getString("queueID"),
      scraperConfig.getString("ttw").toInt.seconds,
      scraperConfig.getString("userAgent"))
    while (true) {
      redisQApi.stream().foreach {
        case Right(res) => DBController.pushToDB(res)
        case Left(ex: ParsingFailure) => logger.warn(s"Error deserializing json object, cause: ${ex.underlying.getCause}" + s" offending json: ${ex.message}")
        case Left(ex: Throwable) if NonFatal(ex) => logger.error(s"General run exception", ex)
      }
    }
  }
}