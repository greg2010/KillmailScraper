package killmailscraper

import java.net.SocketTimeoutException
import java.sql.Timestamp
import java.text.{ParseException, SimpleDateFormat}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.JdbcBackend._
import db.models.Tables.profile.api._
import slick.jdbc.TransactionIsolation.ReadCommitted
import spray.json._
import KillmailPackage._
import com.typesafe.config.Config
import db.models.Tables.{CharacterRow, Attackers => DBAttackers, Killmail => DBKillmail, _}
import org.http4s.Uri
import org.http4s.client.blaze.PooledHttp1Client


class Scrape(db: DatabaseDef, config: Config) extends LazyLogging {
  private val kmEndpoint: Uri = Uri.unsafeFromString(s"https://redisq.zkillboard.com/listen.php?" +
    s"queueID=${config.getString("queueID")}&" +
    s"ttw=${config.getInt("ttw")}")

  def run(): Unit = {
    val httpClient = PooledHttp1Client()
    val getKillmail = httpClient.expect[String](kmEndpoint)
    def next(): Unit = {
      try {
        val s = getKillmail.unsafePerformSyncFor(1.seconds)
        parseJson(s)
      } catch {
        case (exc: NullPackageException) => logger.warn(exc.toString, exc)
        case (exc: SocketTimeoutException) => logger.warn(s"Socket timeout", exc)
        case (exc) => logger.error("General run exception", exc)
      }
      next()
    }
    next()
  }

  private def parseJson(s: String): Future[Unit] = {
    val f = Future {
      val json = s.parseJson.convertTo[RootPackage]
      pushToDB(json.`package`)
    }
    f.onFailure {
      case (exc: DeserializationException) => logger.warn(s"Error deserializng json object, cause: ${exc.cause}," +
        s" fieldNames: ${exc.fieldNames}, message: ${exc.msg}", exc)
      case (exc) => logger.error("General getDeserializedJson exception", exc)
    }
    f
  }

  private def getKillmailRow(pkg: KillPackage): Future[KillmailRow] = {
    val f = Future {
      val km = pkg.killmail
      val timestamp: Timestamp = new Timestamp(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(km.killTime).getTime)
      val now: Timestamp = new Timestamp(System.currentTimeMillis())
      val finalBlowId: Option[Int] = km.attackers.find(attacker => attacker.finalBlow) match {
        case Some(attacker) => attacker.character match {
          case Some(character) => Some(character.id)
          case None => None
        }
        case None => None
      }

      val victimId: Option[Int] = km.victim.character match {
        case Some(x) => Option(x.id)
        case None => None
      }

      // TODO: handle no finalBlow case, no victim charID
      KillmailRow(killId = pkg.killID,
        shipId = km.victim.shipType.id,
        characterId = victimId,
        solarsystemId = km.solarSystem.id,
        killTime = timestamp,
        attackerCount = km.attackerCount,
        finalBlow = finalBlowId,
        positionX = km.victim.position.x,
        positionY = km.victim.position.y,
        positionZ = km.victim.position.z,
        addedAt = now)
    }
    f.onFailure {
      case (exc: ParseException) => logger.warn(s"Failed to parse killTime=${pkg.killmail.killTime}", exc)
      case (exc) => logger.error("General exception in getKillmailsRow", exc)
    }
    f
  }

  private def getAttackersRowList(pkg: KillPackage): Future[Seq[AttackersRow]] = {

    val f = Future {
      val km = pkg.killmail
      km.attackers map { attacker =>
        val attackerId: Option[Int] = attacker.character match {
          case Some(char) => Some(char.id)
          case None => None
        }
        val shipId: Option[Int] = attacker.shipType match {
          case Some(ship) => Some(ship.id)
          case None => None
        }
        val weaponId: Option[Int] = attacker.weaponType match {
          // Spicy pattern matching
          case Some(weapon) => weapon.id match {
            case Some(id) => Some(id)
            case None => shipId
          }
          case None => shipId
        }

        AttackersRow(killId = pkg.killID,
          shipId = shipId,
          characterId = attackerId,
          weapontypeId = weaponId,
          damageDone = attacker.damageDone,
          securityStatus = attacker.securityStatus)
      }
    }
    f.onFailure {
      case (exc) => logger.error("General exception in getAttackersRowList", exc)
    }
    f
  }

  private def getCharacterRowList(pkg: KillPackage): Future[Seq[CharacterRow]] = {
    val f = Future {
      val km = pkg.killmail
      val now: Timestamp = new Timestamp(System.currentTimeMillis())

      val charList = km.attackers.filter(_.character.isDefined) map { attacker =>
        CharacterRow(characterId = attacker.character.get.id,
          corporationId = attacker.corporation.get.id,
          name = attacker.character.get.name,
          lastUpdated = now)
      }
      km.victim.character match {
        case Some(x) => charList :+ CharacterRow(characterId = km.victim.character.get.id,
          corporationId = km.victim.corporation.id,
          name = km.victim.character.get.name,
          lastUpdated = now)
        case None => charList
      }
    }
    f.onFailure {
      case (exc) => logger.error("General exception in getAttackersRowList", exc)
    }
    f
  }

  private def getItemRowList(pkg: KillPackage): Future[Option[Seq[ItemTypeRow]]] = {
    val f = Future {
      pkg.killmail.victim.Items match {
        case Some(items) => {
          val km = pkg.killmail
          // TODO: rename(?) itemType
          Some(
            items.map { item =>
              ItemTypeRow(killId = pkg.killID,
                itemId = item.itemType.id,
                quantityDropped = item.quantityDropped.getOrElse(0),
                quantityDestroyed = item.quantityDestroyed.getOrElse(0))
            }
          )
        }
        case None => None
      }
    }
    f.onFailure {
      case (exc) => logger.error("General exception in getItemRowList", exc)
    }
    f
  }

  private def getZkbMetadataRow(pkg: KillPackage): Future[ZkbMetadataRow] = {
    val f = Future {
      ZkbMetadataRow(killId = pkg.killID, locationId = pkg.zkb.locationID, hash = pkg.zkb.hash,
        totalValue = pkg.zkb.totalValue, points = pkg.zkb.points)
    }
    f.onFailure {
      case (exc) => logger.error("General exception in getZkbMetadataRow", exc)
    }
    f
  }

  def pushToDB(pkg: KillPackage): Unit = {
    // TODO: figure out what to do with corporation table
      val kmRow = getKillmailRow(pkg)
      val atRow = getAttackersRowList(pkg)
      val chRow = getCharacterRowList(pkg)
      val itemRow = getItemRowList(pkg)
      val zkbRow = getZkbMetadataRow(pkg)

      val composedQuery: Future[(KillmailRow, Seq[AttackersRow], Seq[CharacterRow], Option[Seq[ItemTypeRow]], ZkbMetadataRow)] =
        for {
          killmailRow <- kmRow
          attackersRowList <- atRow
          characterRowList <- chRow
          itemRowList <- itemRow
          zkbMetadataRow <- zkbRow
        } yield (killmailRow, attackersRowList, characterRowList, itemRowList, zkbMetadataRow)


      composedQuery.flatMap { rows =>
        val characterUpsert = DBIO.sequence(rows._3 map { charRow =>
          Character.insertOrUpdate(charRow)
        })

        logger.debug(s"Building db objects for km #${rows._1.killId} is complete. Trying to push to db.")

        val query = (for {
          insertIntoKillmailAction <- DBKillmail += rows._1
          insertIntoAttackersAction <- DBAttackers ++= rows._2
          insertIntoItemsAction <- ItemType ++= rows._4.getOrElse(None)
          insertIntoZkbAction <- ZkbMetadata += rows._5
        } yield ()).transactionally.withTransactionIsolation(ReadCommitted)


        db.run(query) flatMap { result =>
          logger.info(s"Killmail with killId=${rows._1.killId} was succesfully pushed to db.")
          Future(result)
        }
        db.run(characterUpsert) flatMap { result =>
          logger.info(s"Character(s) ${rows._3.map(_.characterId)} are upserted to DB")
          Future(result)
        }
      } onComplete {
        case Success(res) => res
        case Failure(ex) => logger.error(s"Failed DB transaction, killId=${pkg.killID}", ex)
      }
  }
}