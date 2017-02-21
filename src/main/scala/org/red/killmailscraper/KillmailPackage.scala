package org.red.killmailscraper



import spray.json._


case class RootPackage(`package`: KillPackage)
case class NullPackage(`package`: String) // Currently not used

case class KillPackage(killID: Long,
                       killmail: Killmail,
                       zkb: Zkb)

case class Killmail(solarSystem: EntityDef,
                    killID: Long,
                    killTime: String,
                    attackers: List[Attacker],
                    attackerCount: Long,
                    victim: Victim)

case class EntityDef(id: Long, name: String)
case class WeaponType(id: Option[Long], name: String) // id is [None] if WeaponType is Ship
case class SolarSystem(id: Long)

case class Attacker(character: Option[EntityDef], // Null if attacker is NPC
                    corporation: Option[EntityDef], // Null if NPC
                    alliance: Option[EntityDef], // Null if no alliance
                    shipType: Option[EntityDef], // Null if NPC (?)
                    weaponType: Option[WeaponType], // Null if unknown (?)
                    damageDone: Long,
                    finalBlow: Boolean,
                    securityStatus: Double)

case class Position(y: Double, x: Double, z: Double)
case class Item(itemType: EntityDef,
                quantityDestroyed: Option[Long], // Null if none destroyed
                quantityDropped: Option[Long]) // Null if none dropped

case class Victim(character: Option[EntityDef], // Null if structure
                  corporation: EntityDef,
                  alliance: Option[EntityDef], // Null if no alliance
                  shipType: EntityDef,
                  damageTaken: Long,
                  items: Option[List[Item]],
                  position: Position)

case class Zkb(locationID: Long,
               hash: String,
               totalValue: Double,
               points: Long)

case class NullPackageException(message: String) extends RuntimeException

object KillmailPackage extends DefaultJsonProtocol {
  implicit object PackageJson extends JsonReader[RootPackage] {
    def read(value: JsValue): RootPackage = value.asJsObject.getFields("package").head match {
      case JsNull => throw NullPackageException(s"KillPackage expected, got null")
      case x => RootPackage(x.toJson.convertTo[KillPackage])
    }
  }
  implicit val nullPackage: JsonFormat[NullPackage] = lazyFormat(jsonFormat1(NullPackage))
  implicit val killPackage: JsonFormat[KillPackage] = lazyFormat(jsonFormat3(KillPackage))
  implicit val killmail: JsonFormat[Killmail] = lazyFormat(jsonFormat6(Killmail))
  implicit val weaponType: JsonFormat[WeaponType] = lazyFormat(jsonFormat2(WeaponType))
  implicit val entityDef: JsonFormat[EntityDef] = lazyFormat(jsonFormat2(EntityDef))
  implicit val solarSystem: JsonFormat[SolarSystem] = lazyFormat(jsonFormat1(SolarSystem))
  implicit val attackers: JsonFormat[Attacker] = lazyFormat(jsonFormat8(Attacker))
  implicit val position: JsonFormat[Position] = lazyFormat(jsonFormat3(Position))
  implicit val item: JsonFormat[Item] = lazyFormat(jsonFormat3(Item))
  implicit val victim: JsonFormat[Victim] = lazyFormat(jsonFormat7(Victim))
  implicit val zkb: JsonFormat[Zkb] = lazyFormat(jsonFormat4(Zkb))
}
