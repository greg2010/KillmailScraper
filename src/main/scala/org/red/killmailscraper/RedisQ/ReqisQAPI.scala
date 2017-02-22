package org.red.killmailscraper.RedisQ

import com.typesafe.scalalogging.LazyLogging
import io.circe.DecodingFailure
import org.http4s.{InvalidMessageBodyFailure, Uri}
import org.http4s.circe._
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze._
import org.red.killmailscraper.RedisQ.RedisQSchema._
import org.red.killmailscraper.scraperConfig
import io.circe.generic.auto._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scalaz.{-\/, \/-}


class ReqisQAPI(queueId: String) extends LazyLogging {

  private val url: Uri = Uri.unsafeFromString("https://redisq.zkillboard.com/listen.php?" +
    s"queueID=${queueId}&" +
    s"ttw=${scraperConfig.getInt("ttw")}")
  private val httpClient = SimpleHttp1Client()

  def poll(): KillPackage = {

    @tailrec def next(): KillPackage = {
      implicit val jdec = jsonOf[RootPackage]
      val getKillmail = httpClient.expect[RootPackage](url)
      getKillmail.unsafePerformSyncAttemptFor((scraperConfig.getInt("ttw") + 2).seconds) match {
        case -\/(e) => e match {
          case ex: UnexpectedStatus if ex.status.code == 429 => {
            val sleepTime: Int = scraperConfig.getInt("ttw") / 2
            logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
            Thread.sleep(sleepTime.seconds.toMillis)
            next()
          }
          case ex: InvalidMessageBodyFailure => {
            logger.warn(s"Error deserializing json object, cause: ${ex.cause}" +
              s" offending json: ${ex.message}")
            next()
          }
          case ex if NonFatal(ex) => {
            logger.error(s"General run exception", ex)
            next()
          }
        }
        case \/-(response) => response.`package` match {
          case Some(x) => x
          case None => next()
        }
      }
    }

    next()
  }
}