package killmailscraper


import killmailscraper.KillmailPackage.lazyFormat
import spray.json._


/*case class KillPackage(killID: Int, killmail: Killmail, zkb: Zkb)
case class Killmail(solarSystem: SolarSystem, killID: Int, killTime: String,
                    attackers: JsValue, attackerCount: Int, victim: Victim)
case class SolarSystem()
case class ShipType()
case class Character()
case class Corporation()
case class Alliance()
case class Attackers()
case class Victim()
case class Items()
case class Position()
case class Zkb()*/

case class RootPackage(`package`: KillPackage)

case class KillPackage(killID: Int,
                       killmail: Killmail,
                       zkb: Zkb)

case class Killmail(solarSystem: SolarSystem,
                    killID: Int,
                    killTime: String,
                    attackers: List[Attackers],
                    attackerCount: Int,
                    victim: Victim)

case class EntityDef(id: Int, name: String)

case class WeaponType(id: Option[Int], name: String) // Because CCP doesn't know how to design APIs

case class SolarSystem(id: Int)

case class Attackers(character: Option[EntityDef],
                     weaponType: Option[WeaponType],
                     damageDone: Long,
                     finalBlow: Boolean,
                     securityStatus: Double)

case class Position(y: Double, x: Double, z: Double)

case class Item(itemType: EntityDef, quantityDestroyed: Option[Int], quantityDropped: Option[Int])

case class Victim(character: Option[EntityDef],
                  corporation: EntityDef,
                  alliance: Option[EntityDef],
                  shipType: EntityDef,
                  damageTaken: Long,
                  Items: Option[List[Item]],
                  position: Position)

case class Zkb(locationID: Int,
               hash: String,
               totalValue: Double,
               points: Int)

object KillmailPackage extends DefaultJsonProtocol {
  implicit val packageFormat: JsonFormat[RootPackage] = lazyFormat(jsonFormat1(RootPackage))
  implicit val killPackage: JsonFormat[KillPackage] = lazyFormat(jsonFormat3(KillPackage))
  implicit val killmail: JsonFormat[Killmail] = lazyFormat(jsonFormat6(Killmail))
  implicit val weaponType: JsonFormat[WeaponType] = lazyFormat(jsonFormat2(WeaponType))
  implicit val entityDef: JsonFormat[EntityDef] = lazyFormat(jsonFormat2(EntityDef))
  implicit val solarSystem: JsonFormat[SolarSystem] = lazyFormat(jsonFormat1(SolarSystem))
  implicit val attackers: JsonFormat[Attackers] = lazyFormat(jsonFormat5(Attackers))
  implicit val position: JsonFormat[Position] = lazyFormat(jsonFormat3(Position))
  implicit val item: JsonFormat[Item] = lazyFormat(jsonFormat3(Item))
  implicit val victim: JsonFormat[Victim] = lazyFormat(jsonFormat7(Victim))
  implicit val zkb: JsonFormat[Zkb] = lazyFormat(jsonFormat4(Zkb))
}
