package org.red.killmailscraper

import javax.net.ssl.SSLContext

import com.typesafe.scalalogging.LazyLogging
import org.http4s.InvalidMessageBodyFailure
import org.http4s.client.blaze.{BlazeClientConfig, SimpleHttp1Client}
import org.red.zkb4s.RedisQ._

import scala.util.control.NonFatal
import scala.concurrent.duration._

class ScraperController extends LazyLogging {

  def run(): Unit = {
    val redisQApi = new ReqisQAPI(scraperConfig.getString("queueID"),
      scraperConfig.getString("ttw").toInt.seconds,
      scraperConfig.getString("userAgent"))

    implicit val c = SimpleHttp1Client(
      config = BlazeClientConfig.defaultConfig.copy(
        sslContext = Some(SSLContext.getDefault),
        endpointAuthentication = true))
    while (true) {
      try {
        redisQApi.stream().foreach { response =>
          DBController.pushToDB(response)
        }
      } catch {
        case ex: InvalidMessageBodyFailure =>
          logger.warn(s"Error deserializing json object, cause: ${ex.cause}" + s" offending json: ${ex.message}")
        case ex if NonFatal(ex) =>
          logger.error(s"General run exception", ex)
      }
    }
  }
}