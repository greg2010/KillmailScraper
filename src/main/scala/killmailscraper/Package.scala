package killmailscraper

import com.typesafe.scalalogging._


object Package
  extends App
    with LazyLogging
    with SlickCodegen.Env{
  val scrape = new Scrape(db)
  val iterations = 100
  def recurse(count: Int): Unit = {
    if (count == 0) Unit
    val json = scrape.getJson
    println(json.prettyPrint)
    scrape.pushToDB(scrape.getJson)
    recurse(count - 1)
  }
  recurse(iterations)
}