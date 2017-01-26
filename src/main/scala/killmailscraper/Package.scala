package killmailscraper

import com.typesafe.scalalogging._


object Package
  extends App
  with LazyLogging {
  val scrape = new Scrape()
 // val json = scrape.getJson
 // println(json.prettyPrint)
}