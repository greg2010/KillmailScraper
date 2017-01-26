package killmailscraper

import com.typesafe.scalalogging._


object killmailscraper
  extends App
  with LazyLogging {
  val scrape = new Scrape()
  val json = scrape.getJson
  println(json.prettyPrint)
}