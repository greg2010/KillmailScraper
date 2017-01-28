package killmailscraper

import java.net.SocketTimeoutException

import slick.jdbc.JdbcBackend.DatabaseDef
import spray.json._

import scalaj.http._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util._
import KillmailPackage._

class Scrape (db: DatabaseDef) extends LazyLogging {
  private val kmEndpoint: String = "https://redisq.zkillboard.com/listen.php"
  def getDeserializedJson: Future[RootPackage] = {
    val f: Future[RootPackage] = Future {
      val httpResponse = Http(kmEndpoint)
        .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
        .asString.body
      logger.info(s"Got response $httpResponse")
      val parsedValue = httpResponse.parseJson.convertTo[RootPackage]
      logger.info(s"Succesfully parsed json km with killid ${parsedValue.`package`.killID}")
      parsedValue
    }
    f.onComplete {
      case Success(x) => pushToDB(x)
      case Failure(exc: NullPackageException) => logger.warn(exc.toString, exc)
      case Failure(exc: SocketTimeoutException) => logger.warn(s"Did not get response from remote server")
      case Failure(exc: DeserializationException) => logger.error(s"Error deserializng json object", exc)
      case Failure(exc) => logger.error("General getDeserializedJson exception", exc)
    }
    f
  }

  def pushToDB(killmail: RootPackage): Unit = {
    
  }
}

