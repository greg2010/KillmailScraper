package org.red.killmailscraper

import javax.net.ssl.SSLContext

import com.typesafe.scalalogging.LazyLogging
import org.http4s.InvalidMessageBodyFailure
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client, SimpleHttp1Client}
import org.red.zkb4s.RedisQ._

import scala.util.control.NonFatal
import scala.concurrent.duration._
import scala.util.Try

class ScraperController extends LazyLogging {

  def run(): Unit = {
    val redisQApi = new ReqisQAPI(scraperConfig.getString("queueID"),
      scraperConfig.getString("ttw").toInt.seconds,
      scraperConfig.getString("userAgent"))

    implicit val c = PooledHttp1Client(
      config = BlazeClientConfig.defaultConfig.copy(
        sslContext = Some(SSLContext.getDefault),
        endpointAuthentication = true))
    while (true) {
      Try {
        redisQApi.stream().foreach { response =>
          DBController.pushToDB(response)
        }
      } recover {
        case ex: UnexpectedStatus if ex.status.code == 429 => {
          val sleepTime: Int = scraperConfig.getInt("ttw") / 2
          logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
          Thread.sleep(sleepTime.seconds.toMillis)
        }
        case ex: InvalidMessageBodyFailure => {
          logger.warn(s"Error deserializing json object, cause: ${ex.cause}" +
            s" offending json: ${ex.message}")
        }
        case ex if NonFatal(ex) => {
          logger.error(s"General run exception", ex)
        }
      }
    }
  }
}