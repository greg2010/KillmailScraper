package org.red.killmailscraper

import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import org.red.db.dbAgent
import org.red.db.models.Tables.profile.api._
import org.red.db.models.Tables.{AttackersRow, Character, CharacterRow, Corporation, CorporationRow, ItemType, ItemTypeRow, KillmailRow, ZkbMetadata, ZkbMetadataRow, Attackers => DBAttackers, Killmail => DBKillmail}
import org.red.zkb4s.schema.CommonSchemas.{Killmail => APIKillmail}
import slick.jdbc.TransactionIsolation.ReadCommitted

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}


object DBController extends LazyLogging {

  def pushToDB(pkg: APIKillmail): Unit = {
    Future {
      try {
        val kmRow = getKillmailRow(pkg)
        val atRow = getAttackersRowList(pkg)
        val chRow = getCharacterRowList(pkg)
        val corpRow = getCorporationRowList(pkg)
        val itemRow = getItemRowList(pkg)
        val zkbRow = getZkbMetadataRow(pkg)

        val chRowQuery = prepareCharacterUpsert(chRow).transactionally
        val corpRowQuery = prepareCorporationUpsert(corpRow).transactionally

        /*val characterUpsert = DBIO.sequence(chRow map { charRow =>
          Character.insertOrUpdate(charRow)
        })

        val corporationUpsert = DBIO.sequence(corpRow map { corpRow =>
          Corporation.insertOrUpdate(corpRow)
        })*/

        logger.debug(s"Building db objects for km #${kmRow.killId} is complete. Trying to push to db.")

        val query = (for {
          insertIntoKillmailAction <- DBKillmail += kmRow
          insertIntoAttackersAction <- DBAttackers ++= atRow
          insertIntoItemsAction <- ItemType ++= itemRow.getOrElse(None)
          insertIntoZkbAction <- ZkbMetadata += zkbRow
        } yield ()).transactionally.withTransactionIsolation(ReadCommitted)

        blocking {
          dbAgent.run(query) onComplete {
            case Success(x) => logger.info(s"Killmail with killId=${kmRow.killId} was succesfully pushed to db.")
            case Failure(ex) => logger.error(s"Killmail insert failed, killId=${kmRow.killId}", ex)
          }
          dbAgent.run(chRowQuery) onComplete {
            case Success(x) => logger.info(s"Character(s) ${chRow.map(_.characterId)} are upserted to DB")
            case Failure(ex) => logger.error(s"Character upsert failed, killId=${chRow.map(_.characterId)}", ex)
          }
          dbAgent.run(corpRowQuery) onComplete {
            case Success(x) => logger.info(s"Corporation(s) ${corpRow.map(_.corporationId)} are upserted to DB")
            case Failure(ex) => logger.error(s"Corporation upsert failed, killId=${corpRow.map(_.corporationId)}", ex)
          }
        }
      } catch {
        case e: Throwable if NonFatal(e) => logger.error(s"Failed to build DB transaction, killId=${pkg.killId}", e)
      }
    }
  }

  // Using raw SQL for batch upserts since Slick cannot handle it just yet
  private def prepareCharacterUpsert(chRow: Seq[CharacterRow]): SimpleDBIO[Array[Int]] = {
    SimpleDBIO[Array[Int]] { session =>
      val sql  =
        """INSERT INTO character (character_id,corporation_id,last_updated) VALUES (?, ?, ?)
          | ON CONFLICT (character_id) DO UPDATE SET corporation_id = ?, last_updated = ?;""".stripMargin
      val statement = session.connection.prepareStatement(sql)
      chRow.foreach { row =>
        statement.setLong(1, row.characterId)
        row.corporationId match {
          case Some(id) => {
            statement.setLong(2, id)
            statement.setLong(4, id)
          }
          case None => {
            statement.setNull(2, java.sql.Types.BIGINT)
            statement.setNull(4, java.sql.Types.BIGINT)
          }
        }
        statement.setTimestamp(3, row.lastUpdated)
        statement.setTimestamp(5, row.lastUpdated)
        statement.addBatch()
      }
      statement.executeBatch()
    }
  }

  private def prepareCorporationUpsert(corpRow: Seq[CorporationRow]): SimpleDBIO[Array[Int]] = {
    SimpleDBIO[Array[Int]] { session =>
      val sql  =
        """INSERT INTO corporation (corporation_id,alliance_id) VALUES (?, ?)
          | ON CONFLICT (corporation_id) DO UPDATE SET alliance_id = ?;""".stripMargin
      val statement = session.connection.prepareStatement(sql)
      corpRow.foreach { row =>
        statement.setLong(1, row.corporationId)
        row.allianceId match {
          case Some(id) => {
            statement.setLong(2, id)
            statement.setLong(3, id)
          }
          case None => {
            statement.setNull(2, java.sql.Types.BIGINT)
            statement.setNull(3, java.sql.Types.BIGINT)
          }
        }
        statement.addBatch()
      }
      statement.executeBatch()
    }
  }

  private def getKillmailRow(pkg: APIKillmail): KillmailRow = {
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
      positionX = position._1,
      positionY = position._2,
      positionZ = position._3,
      addedAt = now)
  }

  private def getAttackersRowList(pkg: APIKillmail): Seq[AttackersRow] = {
    pkg.attackers map { attacker =>
      val weaponId: Option[Long] = attacker.weaponType match {
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

  private def getCharacterRowList(pkg: APIKillmail): Seq[CharacterRow] = {
    val now: Timestamp = new Timestamp(System.currentTimeMillis())

    val charList = pkg.attackers.filter(_.character.characterId.isDefined) map { attacker =>
      CharacterRow(characterId = attacker.character.characterId.get,
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

  private def getCorporationRowList(pkg: APIKillmail): Seq[CorporationRow] = {
    val rows = pkg.attackers.filter(_.character.corporationId.isDefined).map { attacker =>
      CorporationRow(corporationId = attacker.character.corporationId.get, allianceId = attacker.character.allianceId)
    }
    pkg.victim.character.corporationId match {
      case Some(x) => CorporationRow(corporationId = x, allianceId = pkg.victim.character.allianceId) +: rows
      case None => rows
    }
  }

  def getItemRowList(pkg: APIKillmail): Option[Seq[ItemTypeRow]] = {
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

  private def getZkbMetadataRow(pkg: APIKillmail): ZkbMetadataRow = {
    ZkbMetadataRow(killId = pkg.killId, hash = pkg.zkbMetadata.hash,
      totalValue = pkg.zkbMetadata.totalValue, points = pkg.zkbMetadata.points)
  }
}
