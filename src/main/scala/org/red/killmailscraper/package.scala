package org.red

import java.io.File

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database

package object killmailscraper {

  private val parsedConfig = ConfigFactory.parseFile(new File("src/main/resources/reference.conf"))
  private val config = ConfigFactory.load(parsedConfig)
  val scraperConfig = config.getConfig("killmailscraper")

}