package killmailscraper

import slick.jdbc.JdbcBackend.DatabaseDef

import spray.json._

import scalaj.http._

import KillmailPackage._

class Scrape (db: DatabaseDef) {
  def getJson: JsValue = {
    val rawResp: HttpResponse[String] = Http("https://redisq.zkillboard.com/listen.php")
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 50000)
      .asString
    rawResp.body.parseJson
  }

  def pushToDB(json: JsValue): Unit = {
    val parsedKm = json.convertTo[RootPackage]
    //TODO: Handle null case
  }
}

