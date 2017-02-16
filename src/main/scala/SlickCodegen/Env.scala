package SlickCodegen

import java.io.File

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend._

trait Env {

  protected val parsedConfig = ConfigFactory.parseFile(new File("src/main/resources/reference.conf"))
  protected val config = ConfigFactory.load(parsedConfig)
  protected val db = Database.forConfig("postgres", config)

}