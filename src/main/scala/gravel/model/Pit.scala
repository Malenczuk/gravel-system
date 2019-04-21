package gravel.model

import gravel.error.{Error, _}

/** A pit for holding gravel. */
case class Pit private(grain: Grain, amount: Double) {

  /** Returns either a copy of this Pit with reduced gravel amount or an InsufficientGravelAmount error.
    *
    * InsufficientGravelAmount is returned with value of missing gravel
    * when the gravel amount on the pit is smaller than given amount.
    */
  def take(amount: Double): Either[Error, Pit] = {
    val gravelAmount = Pit.precision(this.amount - amount)
    Either.cond(
      gravelAmount >= 0,
      this.copy(amount = gravelAmount),
      InsufficientGravelAmount(gravelAmount))
  }

  /** Returns a copy of this Pit with added gravel amount. */
  def put(amount: Double): Pit = this.copy(amount = this.amount + Pit.precision(amount))

  /** Returns a copy of this Pit with updated grain interval.
    *
    * Grain Interval can not be reduced.
    */
  def updateGranulation(grain: Grain): Pit = {
    this.copy(grain = Grain(math.min(this.grain.min, grain.min), math.max(this.grain.max, grain.max)))
  }

  /** Checks if this pit grain interval intersects with given grain interval. */
  def intersects(grain: Grain): Boolean =
    this.grain.min > grain.min && containsRight(grain.max) ||
      this.grain.min < grain.max && containsLeft(grain.min) ||
      contains(grain)

  /** Checks if this pit grain interval contains second grain interval. */
  def contains(grain: Grain): Boolean = Pit.contains(this.grain, grain)

  /** Checks if this pit grain left-bounded interval contains grain size. */
  def containsLeft(grain: Double): Boolean = Pit.containsLeft(this.grain, grain)

  /** Checks if this pit grain right-bounded interval contains grain size. */
  def containsRight(grain: Double): Boolean = Pit.containsRight(this.grain, grain)

  override def toString: String = s"grain: $grain amount: $amount"
}

object Pit {
  /** Creates a Pit with a given grain and amount.
    *
    * Automatically sets amount precision.
    *
    * @param grain  the interval of gravel grains on the pit
    * @param amount the amount of gravel on the pit
    *
    */
  def apply(grain: Grain, amount: Double): Pit = new Pit(grain, precision(amount))

  /** Returns Double value with set precision. */
  def precision(value: Double): Double =
    BigDecimal(value).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble

  /** Checks if first grain interval  contains second grain interval. */
  def contains(grain1: Grain, grain2: Grain): Boolean =
    containsLeft(grain1, grain2.min) && containsRight(grain1, grain2.max)

  /** Checks if first grain left-bounded interval contains grain size. */
  def containsLeft(grain1: Grain, grain2: Double): Boolean = grain1.min <= grain2 && grain1.max > grain2

  /** Checks if first grain right-bounded interval contains grain size. */
  def containsRight(grain1: Grain, grain2: Double): Boolean = grain1.min < grain2 && grain1.max >= grain2
}
