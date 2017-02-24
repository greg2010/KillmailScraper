package org.red.killmailscraper.db

import java.sql.Timestamp
import java.text.{ParseException, SimpleDateFormat}

import com.typesafe.scalalogging.LazyLogging
import org.red.killmailscraper.db.models.Tables.profile.api._
import org.red.killmailscraper.db.models.Tables.{AttackersRow, Character, CharacterRow, Corporation, CorporationRow, ItemType, ItemTypeRow, KillmailRow, ZkbMetadata, ZkbMetadataRow, Attackers => DBAttackers, Killmail => DBKillmail}
import org.red.killmailscraper.dbAgent
import org.red.zkb4s.RedisQ.RedisQSchema.KillPackage
import slick.jdbc.TransactionIsolation.ReadCommitted

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object DBController extends LazyLogging {

  def pushToDB(pkg: KillPackage): Unit = {
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
        case Failure(ex) => logger.error(s"Failed to build DB transaction, killId=${pkg.killID}", ex)
      }
  }

  private def getKillmailRow(pkg: KillPackage): Future[KillmailRow] = {
    val f = Future {
      val km = pkg.killmail
      val timestamp: Timestamp = new Timestamp(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(km.killTime).getTime)
      val now: Timestamp = new Timestamp(System.currentTimeMillis())
      val finalBlowId: Option[Long] = km.attackers.find(attacker => attacker.finalBlow) match {
        case Some(attacker) => attacker.character match {
          case Some(character) => Some(character.id)
          case None => None
        }
        case None => None
      }

      val victimId: Option[Long] = km.victim.character match {
        case Some(x) => Option(x.id)
        case None => None
      }

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

    f onComplete {
      case Success(x) => x
      case Failure(ex: ParseException) => logger.warn(s"Failed to parse killTime=${pkg.killmail.killTime}", ex)
      case Failure(ex) => logger.error("General exception in getKillmailsRow", ex)
    }
    f
  }

  private def getAttackersRowList(pkg: KillPackage): Future[Seq[AttackersRow]] = {

    val f = Future {
      val km = pkg.killmail
      km.attackers map { attacker =>
        val attackerId: Option[Long] = attacker.character match {
          case Some(char) => Some(char.id)
          case None => None
        }
        val shipId: Option[Long] = attacker.shipType match {
          case Some(ship) => Some(ship.id)
          case None => None
        }
        val weaponId: Option[Long] = attacker.weaponType match {
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

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getAttackersRowList", ex)
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

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getAttackersRowList", ex)
    }
    f
  }

  private def getCorporationRowList(pkg: KillPackage): Future[Seq[CorporationRow]] = {

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
      val km = pkg.killmail
      val victimAllianceId: Option[Long] = km.victim.alliance match {
        case Some(alliance) => Some(alliance.id)
        case None => None
      }
      km.attackers.filter(x => x.corporation.isDefined).map { attacker =>
        val allianceId: Option[Long] = attacker.alliance match {
          case Some(x) => Some(x.id)
          case None => None
        }
        CorporationRow(corporationId = attacker.corporation.get.id, allianceId = allianceId,
          name = attacker.corporation.get.name)
      } :+ CorporationRow(corporationId = km.victim.corporation.id, allianceId = victimAllianceId,
        name = km.victim.corporation.name)
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getCorporationRowList", ex)
    }
    f
  }

  def getItemRowList(pkg: KillPackage): Future[Option[Seq[ItemTypeRow]]] = {
    val f = Future {
      pkg.killmail.victim.items match {
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

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getItemRowList", ex)
    }
    f
  }

  private def getZkbMetadataRow(pkg: KillPackage): Future[ZkbMetadataRow] = {
    val f = Future {
      ZkbMetadataRow(killId = pkg.killID, locationId = pkg.zkb.locationID, hash = pkg.zkb.hash,
        totalValue = pkg.zkb.totalValue, points = pkg.zkb.points)
    }

    f onComplete {
      case Success(x) => x
      case Failure(ex) => logger.error("General exception in getZkbMetadataRow", ex)
    }
    f
  }
}
