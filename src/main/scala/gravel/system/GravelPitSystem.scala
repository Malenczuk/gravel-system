package gravel.system

import gravel.error.Error
import gravel.model.{Grain, Order, Pit}

/** An implementation Gravel Pit System for producing, storing and ordering gravel. */
trait GravelPitSystem {
  /** Ordering system responsible for logging and creating orders. */
  val orderSystem: OrderSystem

  /** Creates new or updates existing gravel pit.
    *
    * @param grain  the interval of gravel grains
    * @param amount the amount of gravel
    * @return Either a updated list of gravel pits in the system or an error
    **/
  def production(grain: Grain, amount: Double): Either[Error, Seq[Pit]]

  /** Orders a set amount of gravel of give grain interval.
    *
    * @param grain  the interval of gravel grains
    * @param amount the amount of gravel
    * @return either placed order or an error
    **/
  def order(grain: Grain, amount: Double): Either[Error, Order]

  /** Returns either deleted gravel pit or an error. */
  def delete(grain: Grain): Either[Error, Pit]

  /** Returns a list of current gravel pits in the system. */
  def status(): Seq[Pit]
}
