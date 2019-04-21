package gravel.system.impl

import cats.implicits._
import gravel.error.{Error, _}
import gravel.model._
import gravel.system.{GravelPitSystem, OrderSystem}

import scala.collection.mutable.ListBuffer

/** An implementation Gravel Pit System for producing, storing and ordering gravel.
  *
  * @constructor Creates new gravel pit system with a order system
  * @param system the ordering system responsible for logging and creating orders
  */
class BasicGravelPitSystem(system: OrderSystem) extends GravelPitSystem {
  val orderSystem: OrderSystem = system
  val pits: ListBuffer[Pit] = ListBuffer[Pit]()


  /** Creates new or updates existing gravel pit.
    *
    * If there is no existing gravel pits with a grain interval
    * intersecting given grain then a new gravel pit will be created.
    * If there is one gravel pit that intersects given grain
    * it's grain interval will be updated and the gravel amount added to it.
    * In another case an error will occur.
    *
    * @param grain  the interval of gravel grains
    * @param amount the amount of gravel
    * @return Either a updated list of gravel pits in the system or an error
    **/
  override def production(grain: Grain, amount: Double): Either[Error, Seq[Pit]] = {
    val gravelAmount = Pit.precision(amount)
    validate(grain, gravelAmount) match {
      case Some(error) => Either.left(error)
      case None =>
        if (pits.count(_.intersects(grain)) > 1) Either.left(NoValidGravelPit)
        else {
          if (pits.exists(_.intersects(grain))) {
            val pit = pits.find(_.intersects(grain)).get
            val updatedPit = pit.updateGranulation(grain).put(gravelAmount)
            pits.update(pits.indexOf(pit), updatedPit)
          } else {
            pits += Pit(grain, gravelAmount)
          }
          Either.right(pits.toList)
        }
    }
  }

  /** Orders a set amount of gravel of give grain interval.
    *
    * @param grain  the interval of gravel grains
    * @param amount the amount of gravel
    * @return either placed order or an error
    **/
  override def order(grain: Grain, amount: Double): Either[Error, Order] = {
    val gravelAmount = Pit.precision(amount)
    validate(grain, gravelAmount) match {
      case Some(error) => Either.left(error)
      case None =>
        val availablePits = pits.filter(pit => Pit.contains(grain, pit.grain))
        val availableAmount = availablePits.foldLeft(0d) { (acc, pit) => acc + pit.amount }
        if (availableAmount < gravelAmount)
          Either.left(InsufficientGravelAmount(Pit.precision(availableAmount - gravelAmount)))
        else {
          val order = orderSystem.placeOrder(grain, gravelAmount, availablePits)
          order.gravel.foreach {
            pit => availablePits.find(_.grain == pit.grain).get.take(pit.amount).map(pits.update(pits.indexWhere(_.grain == pit.grain), _))
          }

          Either.right(order)
        }
    }
  }

  /** Validates grain interval and gravel amount.
    *
    * NegativeGrain is returned when grain interval contains negative number.
    * NotAValidRange is returned when grain interval size equals 0 or right side is smaller than left side.
    * NegativeGravelAmount is returned when given gravel amount is negative.
    * None is return when none of the above occurs.
    *
    * @param grain  the interval of gravel grains
    * @param amount the amount of gravel
    **/
  private def validate(grain: Grain, amount: Double): Option[Error] = {
    if (grain.min < 0 || grain.max < 0) Some(NegativeGrain)
    else if (grain.min >= grain.max) Some(NotAValidInterval)
    else if (amount < 0) Some(NegativeGravelAmount)
    else None
  }

  /** Returns either deleted gravel pit or an NonExistingGravelPit error.
    *
    * NonExistingGravelPit is returned when there is no gravel pit in the system with given grain interval.
    */
  override def delete(grain: Grain): Either[Error, Pit] =
    pits.find(_.grain == grain) match {
      case Some(pit) =>
        pits -= pit
        Either.right(pit)
      case None => Either.left(NonExistingGravelPit)
    }

  /** Returns a list of current gravel pits in the system. */
  override def status(): Seq[Pit] = pits.toList

}
