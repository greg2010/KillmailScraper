package org.red.killmailscraper.db.models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = SlickCodegen.CustomPostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Attackers.schema, Character.schema, Corporation.schema, ItemType.schema, Killmail.schema, ZkbMetadata.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Attackers
   *  @param killId Database column kill_id SqlType(int8)
   *  @param shipId Database column ship_id SqlType(int8), Default(None)
   *  @param characterId Database column character_id SqlType(int8), Default(None)
   *  @param weapontypeId Database column weapontype_id SqlType(int8), Default(None)
   *  @param damageDone Database column damage_done SqlType(int8)
   *  @param securityStatus Database column security_status SqlType(float8) */
  final case class AttackersRow(killId: Long, shipId: Option[Long] = None, characterId: Option[Long] = None, weapontypeId: Option[Long] = None, damageDone: Long, securityStatus: Double)
  /** GetResult implicit for fetching AttackersRow objects using plain SQL queries */
  implicit def GetResultAttackersRow(implicit e0: GR[Long], e1: GR[Option[Long]], e2: GR[Double]): GR[AttackersRow] = GR{
    prs => import prs._
    AttackersRow.tupled((<<[Long], <<?[Long], <<?[Long], <<?[Long], <<[Long], <<[Double]))
  }
  /** Table description of table attackers. Objects of this class serve as prototypes for rows in queries. */
  class Attackers(_tableTag: Tag) extends profile.api.Table[AttackersRow](_tableTag, "attackers") {
    def * = (killId, shipId, characterId, weapontypeId, damageDone, securityStatus) <> (AttackersRow.tupled, AttackersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(killId), shipId, characterId, weapontypeId, Rep.Some(damageDone), Rep.Some(securityStatus)).shaped.<>({r=>import r._; _1.map(_=> AttackersRow.tupled((_1.get, _2, _3, _4, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column kill_id SqlType(int8) */
    val killId: Rep[Long] = column[Long]("kill_id")
    /** Database column ship_id SqlType(int8), Default(None) */
    val shipId: Rep[Option[Long]] = column[Option[Long]]("ship_id", O.Default(None))
    /** Database column character_id SqlType(int8), Default(None) */
    val characterId: Rep[Option[Long]] = column[Option[Long]]("character_id", O.Default(None))
    /** Database column weapontype_id SqlType(int8), Default(None) */
    val weapontypeId: Rep[Option[Long]] = column[Option[Long]]("weapontype_id", O.Default(None))
    /** Database column damage_done SqlType(int8) */
    val damageDone: Rep[Long] = column[Long]("damage_done")
    /** Database column security_status SqlType(float8) */
    val securityStatus: Rep[Double] = column[Double]("security_status")

    /** Foreign key referencing Killmail (database name attackers_kill_id_fkey) */
    lazy val killmailFk = foreignKey("attackers_kill_id_fkey", killId, Killmail)(r => r.killId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Attackers */
  lazy val Attackers = new TableQuery(tag => new Attackers(tag))

  /** Entity class storing rows of table Character
   *  @param characterId Database column character_id SqlType(int8), PrimaryKey
   *  @param corporationId Database column corporation_id SqlType(int8)
   *  @param name Database column name SqlType(varchar), Length(50,true)
   *  @param lastUpdated Database column last_updated SqlType(timestamp) */
  final case class CharacterRow(characterId: Long, corporationId: Long, name: String, lastUpdated: java.sql.Timestamp)
  /** GetResult implicit for fetching CharacterRow objects using plain SQL queries */
  implicit def GetResultCharacterRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[CharacterRow] = GR{
    prs => import prs._
    CharacterRow.tupled((<<[Long], <<[Long], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table character. Objects of this class serve as prototypes for rows in queries. */
  class Character(_tableTag: Tag) extends profile.api.Table[CharacterRow](_tableTag, "character") {
    def * = (characterId, corporationId, name, lastUpdated) <> (CharacterRow.tupled, CharacterRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(characterId), Rep.Some(corporationId), Rep.Some(name), Rep.Some(lastUpdated)).shaped.<>({r=>import r._; _1.map(_=> CharacterRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column character_id SqlType(int8), PrimaryKey */
    val characterId: Rep[Long] = column[Long]("character_id", O.PrimaryKey)
    /** Database column corporation_id SqlType(int8) */
    val corporationId: Rep[Long] = column[Long]("corporation_id")
    /** Database column name SqlType(varchar), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_updated")
  }
  /** Collection-like TableQuery object for table Character */
  lazy val Character = new TableQuery(tag => new Character(tag))

  /** Entity class storing rows of table Corporation
   *  @param corporationId Database column corporation_id SqlType(int8), PrimaryKey
   *  @param allianceId Database column alliance_id SqlType(int8), Default(None)
   *  @param name Database column name SqlType(varchar), Length(50,true) */
  final case class CorporationRow(corporationId: Long, allianceId: Option[Long] = None, name: String)
  /** GetResult implicit for fetching CorporationRow objects using plain SQL queries */
  implicit def GetResultCorporationRow(implicit e0: GR[Long], e1: GR[Option[Long]], e2: GR[String]): GR[CorporationRow] = GR{
    prs => import prs._
    CorporationRow.tupled((<<[Long], <<?[Long], <<[String]))
  }
  /** Table description of table corporation. Objects of this class serve as prototypes for rows in queries. */
  class Corporation(_tableTag: Tag) extends profile.api.Table[CorporationRow](_tableTag, "corporation") {
    def * = (corporationId, allianceId, name) <> (CorporationRow.tupled, CorporationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(corporationId), allianceId, Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> CorporationRow.tupled((_1.get, _2, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column corporation_id SqlType(int8), PrimaryKey */
    val corporationId: Rep[Long] = column[Long]("corporation_id", O.PrimaryKey)
    /** Database column alliance_id SqlType(int8), Default(None) */
    val allianceId: Rep[Option[Long]] = column[Option[Long]]("alliance_id", O.Default(None))
    /** Database column name SqlType(varchar), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
  }
  /** Collection-like TableQuery object for table Corporation */
  lazy val Corporation = new TableQuery(tag => new Corporation(tag))

  /** Entity class storing rows of table ItemType
   *  @param killId Database column kill_id SqlType(int8)
   *  @param itemId Database column item_id SqlType(int8)
   *  @param quantityDropped Database column quantity_dropped SqlType(int8)
   *  @param quantityDestroyed Database column quantity_destroyed SqlType(int8) */
  final case class ItemTypeRow(killId: Long, itemId: Long, quantityDropped: Long, quantityDestroyed: Long)
  /** GetResult implicit for fetching ItemTypeRow objects using plain SQL queries */
  implicit def GetResultItemTypeRow(implicit e0: GR[Long]): GR[ItemTypeRow] = GR{
    prs => import prs._
    ItemTypeRow.tupled((<<[Long], <<[Long], <<[Long], <<[Long]))
  }
  /** Table description of table item_type. Objects of this class serve as prototypes for rows in queries. */
  class ItemType(_tableTag: Tag) extends profile.api.Table[ItemTypeRow](_tableTag, "item_type") {
    def * = (killId, itemId, quantityDropped, quantityDestroyed) <> (ItemTypeRow.tupled, ItemTypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(killId), Rep.Some(itemId), Rep.Some(quantityDropped), Rep.Some(quantityDestroyed)).shaped.<>({r=>import r._; _1.map(_=> ItemTypeRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column kill_id SqlType(int8) */
    val killId: Rep[Long] = column[Long]("kill_id")
    /** Database column item_id SqlType(int8) */
    val itemId: Rep[Long] = column[Long]("item_id")
    /** Database column quantity_dropped SqlType(int8) */
    val quantityDropped: Rep[Long] = column[Long]("quantity_dropped")
    /** Database column quantity_destroyed SqlType(int8) */
    val quantityDestroyed: Rep[Long] = column[Long]("quantity_destroyed")

    /** Foreign key referencing Killmail (database name item_type_kill_id_fkey) */
    lazy val killmailFk = foreignKey("item_type_kill_id_fkey", killId, Killmail)(r => r.killId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ItemType */
  lazy val ItemType = new TableQuery(tag => new ItemType(tag))

  /** Entity class storing rows of table Killmail
   *  @param killId Database column kill_id SqlType(int8), PrimaryKey
   *  @param shipId Database column ship_id SqlType(int8)
   *  @param characterId Database column character_id SqlType(int8), Default(None)
   *  @param solarsystemId Database column solarsystem_id SqlType(int8)
   *  @param killTime Database column kill_time SqlType(timestamp)
   *  @param attackerCount Database column attacker_count SqlType(int8)
   *  @param finalBlow Database column final_blow SqlType(int8), Default(None)
   *  @param positionX Database column position_x SqlType(float8)
   *  @param positionY Database column position_y SqlType(float8)
   *  @param positionZ Database column position_z SqlType(float8)
   *  @param addedAt Database column added_at SqlType(timestamp) */
  final case class KillmailRow(killId: Long, shipId: Long, characterId: Option[Long] = None, solarsystemId: Long, killTime: java.sql.Timestamp, attackerCount: Long, finalBlow: Option[Long] = None, positionX: Double, positionY: Double, positionZ: Double, addedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching KillmailRow objects using plain SQL queries */
  implicit def GetResultKillmailRow(implicit e0: GR[Long], e1: GR[Option[Long]], e2: GR[java.sql.Timestamp], e3: GR[Double]): GR[KillmailRow] = GR{
    prs => import prs._
    KillmailRow.tupled((<<[Long], <<[Long], <<?[Long], <<[Long], <<[java.sql.Timestamp], <<[Long], <<?[Long], <<[Double], <<[Double], <<[Double], <<[java.sql.Timestamp]))
  }
  /** Table description of table killmail. Objects of this class serve as prototypes for rows in queries. */
  class Killmail(_tableTag: Tag) extends profile.api.Table[KillmailRow](_tableTag, "killmail") {
    def * = (killId, shipId, characterId, solarsystemId, killTime, attackerCount, finalBlow, positionX, positionY, positionZ, addedAt) <> (KillmailRow.tupled, KillmailRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(killId), Rep.Some(shipId), characterId, Rep.Some(solarsystemId), Rep.Some(killTime), Rep.Some(attackerCount), finalBlow, Rep.Some(positionX), Rep.Some(positionY), Rep.Some(positionZ), Rep.Some(addedAt)).shaped.<>({r=>import r._; _1.map(_=> KillmailRow.tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column kill_id SqlType(int8), PrimaryKey */
    val killId: Rep[Long] = column[Long]("kill_id", O.PrimaryKey)
    /** Database column ship_id SqlType(int8) */
    val shipId: Rep[Long] = column[Long]("ship_id")
    /** Database column character_id SqlType(int8), Default(None) */
    val characterId: Rep[Option[Long]] = column[Option[Long]]("character_id", O.Default(None))
    /** Database column solarsystem_id SqlType(int8) */
    val solarsystemId: Rep[Long] = column[Long]("solarsystem_id")
    /** Database column kill_time SqlType(timestamp) */
    val killTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("kill_time")
    /** Database column attacker_count SqlType(int8) */
    val attackerCount: Rep[Long] = column[Long]("attacker_count")
    /** Database column final_blow SqlType(int8), Default(None) */
    val finalBlow: Rep[Option[Long]] = column[Option[Long]]("final_blow", O.Default(None))
    /** Database column position_x SqlType(float8) */
    val positionX: Rep[Double] = column[Double]("position_x")
    /** Database column position_y SqlType(float8) */
    val positionY: Rep[Double] = column[Double]("position_y")
    /** Database column position_z SqlType(float8) */
    val positionZ: Rep[Double] = column[Double]("position_z")
    /** Database column added_at SqlType(timestamp) */
    val addedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("added_at")
  }
  /** Collection-like TableQuery object for table Killmail */
  lazy val Killmail = new TableQuery(tag => new Killmail(tag))

  /** Entity class storing rows of table ZkbMetadata
   *  @param killId Database column kill_id SqlType(int8), PrimaryKey
   *  @param locationId Database column location_id SqlType(int8)
   *  @param hash Database column hash SqlType(varchar), Length(50,true)
   *  @param totalValue Database column total_value SqlType(float8)
   *  @param points Database column points SqlType(int8) */
  final case class ZkbMetadataRow(killId: Long, locationId: Long, hash: String, totalValue: Double, points: Long)
  /** GetResult implicit for fetching ZkbMetadataRow objects using plain SQL queries */
  implicit def GetResultZkbMetadataRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Double]): GR[ZkbMetadataRow] = GR{
    prs => import prs._
    ZkbMetadataRow.tupled((<<[Long], <<[Long], <<[String], <<[Double], <<[Long]))
  }
  /** Table description of table zkb_metadata. Objects of this class serve as prototypes for rows in queries. */
  class ZkbMetadata(_tableTag: Tag) extends profile.api.Table[ZkbMetadataRow](_tableTag, "zkb_metadata") {
    def * = (killId, locationId, hash, totalValue, points) <> (ZkbMetadataRow.tupled, ZkbMetadataRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(killId), Rep.Some(locationId), Rep.Some(hash), Rep.Some(totalValue), Rep.Some(points)).shaped.<>({r=>import r._; _1.map(_=> ZkbMetadataRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column kill_id SqlType(int8), PrimaryKey */
    val killId: Rep[Long] = column[Long]("kill_id", O.PrimaryKey)
    /** Database column location_id SqlType(int8) */
    val locationId: Rep[Long] = column[Long]("location_id")
    /** Database column hash SqlType(varchar), Length(50,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(50,varying=true))
    /** Database column total_value SqlType(float8) */
    val totalValue: Rep[Double] = column[Double]("total_value")
    /** Database column points SqlType(int8) */
    val points: Rep[Long] = column[Long]("points")

    /** Foreign key referencing Killmail (database name zkb_metadata_kill_id_fkey) */
    lazy val killmailFk = foreignKey("zkb_metadata_kill_id_fkey", killId, Killmail)(r => r.killId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ZkbMetadata */
  lazy val ZkbMetadata = new TableQuery(tag => new ZkbMetadata(tag))
}