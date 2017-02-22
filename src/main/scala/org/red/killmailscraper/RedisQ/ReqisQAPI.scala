package org.red.killmailscraper.RedisQ

import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Method, Request, Uri}
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze._
import org.red.killmailscraper.RedisQ.RedisQSchema._
import org.red.killmailscraper.RedisQ.RedisQSchema.RedisQJsonDeserializer._
import spray.json._
import org.red.killmailscraper.scraperConfig

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scalaz.{-\/, \/-}


class ReqisQAPI(queueId: String) extends LazyLogging {

  private val url: Uri = Uri.unsafeFromString("https://redisq.zkillboard.com/listen.php?" +
    s"queueID=${queueId}&" +
    s"ttw=${scraperConfig.getInt("ttw")}")
  private val httpClient = SimpleHttp1Client()
  private val getKillmail = httpClient.expect[String](url)

  def poll(): KillPackage = {
    @tailrec def next(): KillPackage = {
      getKillmail.unsafePerformSyncAttemptFor((scraperConfig.getInt("ttw") + 2).seconds) match {
        case -\/(e) => {
          e match {
            case ex: UnexpectedStatus if ex.status.code == 429 => {
              val sleepTime: Int = scraperConfig.getInt("ttw") / 2
              logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
              Thread.sleep(sleepTime.seconds.toMillis)
              next()
            }
            case ex: DeserializationException => {
              logger.warn(s"Error deserializing json object, cause: ${ex.cause}" +
                s" fieldNames: ${ex.fieldNames} message: ${ex.msg}")
              next()
            }
            case ex if NonFatal(ex) => {
              logger.error(s"General run exception", ex)
              next()
            }
          }
        }
        case \/-(response) => response
          .parseJson.convertTo[RootPackage].`package` match {
          case Some(x) => x
          case None => next()
        }
      }
    }
    next()
  }
}