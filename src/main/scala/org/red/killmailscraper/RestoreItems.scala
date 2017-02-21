package org.red.killmailscraper
/*
import org.red.killmailscraper.RedisQ.RedisQSchema._
import java.net.SocketTimeoutException

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.http4s.Uri
import db.models.Tables._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.http4s.client.UnexpectedStatus
import org.http4s.client.blaze.SimpleHttp1Client

import scala.util.control.NonFatal
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class RestoreItems(config: Config) extends LazyLogging {
  val httpClient = SimpleHttp1Client()

  def run(): Unit = {
    def queryKillmailItems(queryList: Long): Unit = {
      try {
        val km: String = getKillmail(queryList)
        KillmailStorage.getItemRowList(km.parseJson.convertTo[KillPackage]).onComplete {
          case Success(x) => {
            val q = for {
              itemRowList <- ItemType ++= x.getOrElse(None)
            } yield itemRowList
            dbAgent.run(q)
          }
          case Failure(e) => throw e
        }
      } catch {
        case ex: UnexpectedStatus if ex.status.code == 429 => {
          val sleepTime: Int = config.getInt("ttw") / 2
          logger.warn(s"Got unexpected status exception, sleeping for ${sleepTime.seconds.toMillis} milliseconds...", ex)
          Thread.sleep(sleepTime.seconds.toMillis)
        }
        case (ex: SocketTimeoutException) => logger.warn(s"Socket timeout", ex)
        case ex if NonFatal(ex) => logger.error("General run exception", ex)
      }
    }
    val query = sql"""
                  SELECT t1.kill_id FROM killmail t1 LEFT JOIN item_type t2 ON t2.kill_id = t1.kill_id WHERE t2.kill_id IS NULL;
      """.as[Long]
    dbAgent.run(query).flatMap {x =>
      x.foreach { y =>
        queryKillmailItems(y)
      }
      Future()
    }
  }

  def getKillmail(id: Long): String = {
    val uri = Uri.unsafeFromString(s"https://zkillboard.com/api/kills/killID/${id}/")
    httpClient.expect[String](uri).unsafePerformSyncFor(2.seconds)
  }
}
*/