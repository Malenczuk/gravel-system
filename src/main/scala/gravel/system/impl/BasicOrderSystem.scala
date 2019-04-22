package gravel.system.impl

import gravel.model.{Grain, Order, Pit}
import gravel.system.OrderSystem

import scala.annotation.tailrec

/** An implementation of Order System for logging and creating orders. */
class BasicOrderSystem extends OrderSystem {
  /** Weights of for calculating pit metric.
    *
    * [[grainWeight]] - weight of the size of the pit grain interval
    * [[orderedWeight]] - weight of the orders of the pit
    * [[frequencyWeight]] - weight of the orders from the newest
    * [[leftPartWeight]] - weight of the left amount of gravel after taking it for the order
    * [[amountThreshold]] - threshold of the gravel amount taken form the pit
    * [[metricDiffThreshold]] - threshold of the metric changes
    * */
  val grainWeight: Double = 0.5
  val orderedWeight: Double = 2
  val frequencyWeight: Double = 0.8
  val leftPartWeight: Double = 1.5
  val amountThreshold = 0.05
  val metricDiffThreshold = 0.0001

  /** Creates and logs a new order.
    *
    * Based on metric of each pit decides on their share in current order.
    *
    * @param grain  the interval of ordered gravel grain
    * @param amount the amount of ordered gravel
    * @param pits   the sequence of gravel pits available for fulfilling the order
    */
  def placeOrder(grain: Grain, amount: Double, pits: Seq[Pit]): Order = {
    val pickedPits = adaptivePicking(pits.toList, amount)
      .map(entry => Pit(entry._1, entry._2)).toList
    val order = Order(grain, amount, pickedPits)
    postedOrders += order

    order
  }

  /** Chooses how much gravel take and from witch gravel pits to fulfill the order.
    *
    * This iterative method breaks down the total amount of gravel to smaller amounts
    * and the secrets them to the best possibles pits.
    * If the amount size threshold has been met or additional breaking down of amount sizes
    * does not increase the overall metric over the threshold the previous result will be returned.
    *
    * @param pits     the sequence of gravel pits available for fulfilling the order
    * @param total    the total amount of ordered gravel
    * @param amounts  the amounts of gravel which is taken from gravel pits (adds up to total amount)
    * @param previous the result of previous recursive execution of this method
    * @return map of grain interval and its amount
    */
  @tailrec
  final def adaptivePicking(pits: List[Pit], total: Double, amounts: List[Double] = List(), previous: Map[Grain, Double] = Map()): Map[Grain, Double] = {
    if (amounts.isEmpty) {
      adaptivePicking(pits, total, List(total), previous)
    } else if (amounts.last <= total * amountThreshold && previous.nonEmpty) {
      previous
    } else {
      val bestPick = pickBests(pits, amounts, Map())
      val splitAmounts = amounts.foldLeft(List[Double]()) {
        case (acc, amount) if amount == amounts.head => acc ++ split(amount, pits.length)
        case (acc, amount) => amount :: acc
      }.sortWith(_ > _)

      bestPick match {
        case Some(pick) if previous.nonEmpty =>
          val metricDiff = metric(pits, pick).get - metric(pits, previous).get
          if (metricDiff >= metricDiffThreshold) {
            adaptivePicking(pits, total, splitAmounts, pick)
          } else {
            previous
          }
        case _ =>
          adaptivePicking(pits, total, splitAmounts, bestPick.getOrElse(previous))
      }
    }
  }


  /** Picks best pit for subsequent gravel amounts.
    *
    * @param pits    the sequence of gravel pits available for fulfilling the order
    * @param amounts the amounts of gravel which is taken from gravel pits (adds up to total amount)
    * @param taken   the already taken gravel amounts of given size
    * @return map of grain interval and its amount if distribution is possible
    */
  @tailrec
  final def pickBests(pits: List[Pit], amounts: List[Double], taken: Map[Grain, Double]): Option[Map[Grain, Double]] = {
    if (amounts.isEmpty) {
      Some(taken)
    } else {
      pickBest(pits, amounts.head, taken) match {
        case Some(take) => pickBests(pits, amounts.tail, take)
        case None => None
      }
    }
  }

  /** Picks best pit for given gravel amounts.
    *
    * @param pits   the sequence of gravel pits available for fulfilling the order
    * @param amount the amount of gravel which is taken from gravel pit
    * @param taken  the already taken gravel amounts of given size
    * @return map of grain interval and its amount if distribution is possible
    */
  def pickBest(pits: List[Pit], amount: Double, taken: Map[Grain, Double]): Option[Map[Grain, Double]] = {
    val picks = pits.map { pit: Pit =>
      val take = taken.updated(pit.grain, Pit.precision(amount + taken.getOrElse(pit.grain, 0d)))
      metric(pits, take).flatMap(Some(take, _))
    }
    val bestPick = picks.foldLeft(None: Option[(Map[Grain, Double], Double)]) { (acc, pick) =>
      (acc, pick) match {
        case (Some(best), Some(p)) =>
          if (best._2 > p._2) Some(best)
          else Some(p)
        case _ => pick
      }
    }

    bestPick.map(_._1)
  }

  /** Calculates a metric for all of gravel pit.
    *
    * @param pits  the sequence of gravel pits available for fulfilling the order
    * @param taken the already taken gravel amounts of given size
    * @return metric if its possible
    */
  def metric(pits: List[Pit], taken: Map[Grain, Double]): Option[Double] = {
    val metrics = taken.map { entry: (Grain, Double) =>
      metric(pits.find(_.grain == entry._1).get, entry._2)
    }
    metrics.fold(Option(0d))((acc, metric) => for (m <- metric; a <- acc) yield {
      a + m
    })
  }

  /** Calculates a metric for give gravel pit.
    *
    * @param pit    the gravel pit
    * @param amount the amount of ordered gravel
    */
  def metric(pit: Pit, amount: Double): Option[Double] = {
    val leftAmount = Pit.precision(pit.amount - amount)
    if (leftAmount < 0) None
    else {
      val grainMetric = grainWeight * pit.grain.size
      val orderedMetric = 1 + orderedWeight * allOrdersShare(pit).foldRight((0.0, 1.5)) { (orderPart, acc) =>
        (orderPart * acc._2 + acc._1, acc._2 * frequencyWeight)
      }._1
      val leftPartMetric = math.log1p(100 * leftAmount / pit.amount) * leftPartWeight

      Some(leftPartMetric / orderedMetric / grainMetric)
    }
  }

  /** Splits a double into given amount of approximately equal values. */
  @tailrec
  private final def split(amount: Double, into: Int = 2, acc: List[Double] = List()): List[Double] = {
    if (into == 1) {
      (amount :: acc).sortWith(_ > _)
    } else {
      val a = Pit.precision(amount / into)
      val remaining = Pit.precision(amount - a)
      split(remaining, into - 1, a :: acc)
    }
  }

  /** Calculates share of given pit for each posted order. */
  private def allOrdersShare(pit: Pit): Seq[Double] = {
    postedOrders.map(order =>
      order.gravel.find(_.grain == pit.grain) match {
        case Some(p) => p.amount / order.amount
        case None => 0
      })
  }

}
