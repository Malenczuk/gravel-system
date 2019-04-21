package gravel.system

import gravel.model.{Grain, Order, Pit}

import scala.collection.mutable.ListBuffer

/** A Order System for logging and creating orders. */
trait OrderSystem {
  /** List of already fulfilled orders. */
  val postedOrders: ListBuffer[Order] = ListBuffer[Order]()

  /** Creates and logs a new order.
    *
    * @param grain  the interval of ordered gravel grain
    * @param amount the amount of ordered gravel
    * @param pits   the sequence of gravel pits available for fulfilling the order
    */
  def placeOrder(grain: Grain, amount: Double, pits: Seq[Pit]): Order

  /** Calculates share of given pit for each posted order. */
  def getOrders: List[Order] = postedOrders.toList
}
