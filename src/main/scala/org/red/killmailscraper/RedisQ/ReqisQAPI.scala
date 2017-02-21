package org.red.killmailscraper.RedisQ


import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Method, Request, Uri}
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze._
import org.http4s.circe._
import org.red.killmailscraper.RedisQ.RedisQSchema._
import org.red.killmailscraper.scraperConfig
import io.circe._
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.util.control.NonFatal


class ReqisQAPI(queueId: String) extends LazyLogging {

  private val url: Uri = Uri.unsafeFromString("https://redisq.zkillboard.com/listen.php?" +
    s"queueID=${queueId}&" +
    s"ttw=${scraperConfig.getInt("ttw")}")

  def poll(): KillPackage = {
    implicit val jdec = jsonOf[RootPackage]
    val httpClient = SimpleHttp1Client()
    val getKillmail = httpClient.expect[RootPackage](url)
    def next(tolerance: FiniteDuration): KillPackage = {
      try {
        val response = getKillmail.unsafePerformSyncFor((scraperConfig.getInt("ttw") + 2).seconds)
        response.`package` match {
          case Some(x) => x
          case None => {
            if (tolerance <= 100.milliseconds) next(tolerance)
            else next(tolerance / 2)
          }
        }
      } catch {
        case ex: UnexpectedStatus if ex.status.code == 429 => {
          val sleepTime: Int = scraperConfig.getInt("ttw") / 2
          logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
          Thread.sleep(sleepTime.seconds.toMillis)
          next(tolerance)
        }
        /*case ex: DeserializationException => {
          logger.warn(s"Error deserializng json object, cause: ${ex.cause}" +
            s" fieldNames: ${ex.fieldNames} message: ${ex.msg}")
          next(tolerance)
        }*/
        case ex if NonFatal(ex) => {
          logger.error(s"General run exception, sleeping for ${tolerance.toMillis} milliseconds", ex)
          Thread.sleep(tolerance.toMillis)
          if (tolerance > 10.seconds) next(tolerance)
          else next(tolerance * 2)
        }
      }
    }
    next(500.milliseconds)
  }
}