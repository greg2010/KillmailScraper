package org.red.killmailscraper.db

import java.sql.Timestamp
import java.text.{ParseException, SimpleDateFormat}

import com.typesafe.scalalogging.LazyLogging
import org.red.killmailscraper.db.models.Tables.profile.api._
import org.red.killmailscraper.db.models.Tables.{AttackersRow, Character, CharacterRow, Corporation, CorporationRow, ItemType, ItemTypeRow, KillmailRow, ZkbMetadata, ZkbMetadataRow, Attackers => DBAttackers, Killmail => DBKillmail}
import org.red.killmailscraper.dbAgent
import org.red.zkb4s.CommonSchemas.{ Killmail => APIKillmail }
import slick.jdbc.TransactionIsolation.ReadCommitted

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object DBController extends LazyLogging {

  def pushToDB(pkg: APIKillmail): Unit = {
    val kmRow = getKillmailRow(pkg)
    val atRow = getAttackersRowList(pkg)
    val chRow = getCharacterRowList(pkg)
    val corpRow = getCorporationRowList(pkg)
    val itemRow = getItemRowList(pkg)
    val zkbRow = getZkbMetadataRow(pkg)

    (for {
      killmailRow <- kmRow
      attackersRowList <- atRow
      characterRowList <- chRow
      corporationRowList <- corpRow
      itemRowList <- itemRow
      zkbMetadataRow <- zkbRow
    } yield (killmailRow, attackersRowList, characterRowList, itemRowList, zkbMetadataRow, corporationRowList))
      .onComplete {
        case Success(rows) => {
          val characterUpsert = DBIO.sequence(rows._3 map { charRow =>
            Character.insertOrUpdate(charRow)
          })

          val corporationUpsert = DBIO.sequence(rows._6 map { corpRow =>
            Corporation.insertOrUpdate(corpRow)
          })

          logger.debug(s"Building db objects for km #${rows._1.killId} is complete. Trying to push to db.")

          val query = (for {
            insertIntoKillmailAction <- DBKillmail += rows._1
            insertIntoAttackersAction <- DBAttackers ++= rows._2
            insertIntoItemsAction <- ItemType ++= rows._4.getOrElse(None)
            insertIntoZkbAction <- ZkbMetadata += rows._5
          } yield ()).transactionally.withTransactionIsolation(ReadCommitted)


          dbAgent.run(query) onComplete {
            case Success(x) => logger.info(s"Killmail with killId=${rows._1.killId} was succesfully pushed to db.")
            case Failure(ex) => logger.error(s"Killmail insert failed, killId=${rows._1.killId}", ex)
          }
          dbAgent.run(characterUpsert) onComplete {
            case Success(x) => logger.info(s"Character(s) ${rows._3.map(_.characterId)} are upserted to DB")
            case Failure(ex) => logger.error(s"Character upsert failed, killId=${rows._3.map(_.characterId)}", ex)
          }
          dbAgent.run(corporationUpsert) onComplete {
            case Success(x) => logger.info(s"Corporation(s) ${rows._6.map(_.corporationId)} are upserted to DB")
            case Failure(ex) => logger.error(s"Corporation upsert failed, killId=${rows._6.map(_.corporationId)}", ex)
          }

          Future(rows)
        }
        case Failure(ex) => logger.error(s"Failed to build DB transaction, killId=${pkg.killId}", ex)
      }
  }

  private def getKillmailRow(pkg: APIKillmail): Future[KillmailRow] = {
    val f = Future {
      val timestamp: Timestamp = new Timestamp(pkg.killTime.getTime)
      val now: Timestamp = new Timestamp(System.currentTimeMillis())
      val finalBlowId: Option[Long] = pkg.attackers.find(_.finalBlow) match {
        case Some(attacker) => attacker.character.characterId
        case None => None
      }

      val position: (Option[Double], Option[Double], Option[Double]) = pkg.position match {
        case Some(posn) => (Some(posn.x), Some(posn.y), Some(posn.z))
        case None => (None, None, None)
      }

      KillmailRow(killId = pkg.killId,
        shipId = pkg.victim.shipId,
        characterId = pkg.victim.character.characterId,
        solarsystemId = pkg.solarSystem,
        killTime = timestamp,
        attackerCount = pkg.attackers.length.toLong,
        finalBlow = finalBlowId,
        positionX = position._1, // FIX: after schemas changed
        positionY = position._2,
        positionZ = position._3,
        addedAt = now)
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getKillmailsRow", ex)
    }
    f
  }

  private def getAttackersRowList(pkg: APIKillmail): Future[Seq[AttackersRow]] = {

    val f = Future {
      pkg.attackers map { attacker =>
        val weaponId: Option[Long] = attacker.weaponType match {
          // Spicy pattern matching
          case Some(weapon) => Some(weapon)
          case None => attacker.shipId
        }

        AttackersRow(killId = pkg.killId,
          shipId = attacker.shipId,
          characterId = attacker.character.characterId,
          weapontypeId = weaponId,
          damageDone = attacker.damageDone,
          securityStatus = attacker.securityStatus)
      }
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getAttackersRowList", ex)
    }
    f
  }

  private def getCharacterRowList(pkg: APIKillmail): Future[Seq[CharacterRow]] = {
    val f = Future {
      val now: Timestamp = new Timestamp(System.currentTimeMillis())

      val charList = pkg.attackers.filter(_.character.characterId.isDefined) map { attacker =>
        CharacterRow(characterId = attacker.character.characterId.get, // Drop not null for corp, drop name
          corporationId = attacker.character.corporationId,
          lastUpdated = now)
      }
      pkg.victim.character.characterId match {
        case Some(x) => charList :+ CharacterRow(characterId = x,
          corporationId = pkg.victim.character.corporationId,
          lastUpdated = now)
        case None => charList
      }
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getAttackersRowList", ex)
    }
    f
  }

  private def getCorporationRowList(pkg: APIKillmail): Future[Seq[CorporationRow]] = {

    /*def getCorporationName(corporation: EntityDef): String = {
      try {
        corporation.name match {
          case Some(name) => name
          case None => {
            logger.info(s"Missing corporation name for corporation=${corporation.id} killId=${pkg.killmail.killID}")
            corporationApi
              .getCorporationsNames(List[java.lang.Long](corporation.id.toLong).asJava, config.getString("eveServerName"))
              .get(0).getCorporationName
          }
        }
      } catch {
        case e if NonFatal(e) =>
          logger.error("Failed to query ESI API to get missing corporation name", e)
          throw e
      }
    }*/

    val f = Future {
      val rows = pkg.attackers.filter(_.character.corporationId.isDefined).map { attacker => // Drop name
        CorporationRow(corporationId = attacker.character.corporationId.get, allianceId = attacker.character.allianceId)
      }
      pkg.victim.character.corporationId match {
        case Some(x) => CorporationRow(corporationId = x, allianceId = pkg.victim.character.allianceId) +: rows
        case None => rows
      }
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getCorporationRowList", ex)
    }
    f
  }

  def getItemRowList(pkg: APIKillmail): Future[Option[Seq[ItemTypeRow]]] = {
    val f = Future {
      pkg.victim.items.length match {
        case x if x > 0 => {
          // TODO: rename(?) itemType
          Some(
            pkg.victim.items.map { item =>
              ItemTypeRow(killId = pkg.killId,
                itemId = item.itemId,
                quantityDropped = item.quantityDropped,
                quantityDestroyed = item.quantityDestroyed)
            }
          )
        }
        case _ => None
      }
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getItemRowList", ex)
    }
    f
  }

  private def getZkbMetadataRow(pkg: APIKillmail): Future[ZkbMetadataRow] = {
    val f = Future { // Drop locationId
      ZkbMetadataRow(killId = pkg.killId, hash = pkg.zkbMetadata.hash,
        totalValue = pkg.zkbMetadata.totalValue, points = pkg.zkbMetadata.points)
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getZkbMetadataRow", ex)
    }
    f
  }
}
