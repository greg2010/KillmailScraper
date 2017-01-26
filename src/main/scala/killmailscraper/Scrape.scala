package killmailscraper

import spray.json._
import scalaj.http._

class Scrape {
  private def getJson: JsValue = {
    val rawResp: HttpResponse[String] = Http("https://redisq.zkillboard.com/listen.php").asString
    rawResp.body.parseJson
  }
}
