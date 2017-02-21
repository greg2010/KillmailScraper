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


class ReqisQAPI(queueId: String) extends LazyLogging {

  private val url: Uri = Uri.unsafeFromString("https://redisq.zkillboard.com/listen.php?" +
    s"queueID=${queueId}&" +
    s"ttw=${scraperConfig.getInt("ttw")}")

  def poll(): KillPackage = {
    val httpClient = SimpleHttp1Client()
    val getKillmail = httpClient.expect[String](url)
    @tailrec def next(): KillPackage = {
        val response = getKillmail.unsafePerformSyncFor((scraperConfig.getInt("ttw") + 2).seconds)
          .parseJson.convertTo[RootPackage]
          response.`package` match {
          case Some(x) => x
          case None => next()
        }
    }
    next()
  }
}