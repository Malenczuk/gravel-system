package gravel.model

import java.util.{Calendar, Date}

/** A Order used for storing information of fulfilled order requests. */
case class Order private(grain: Grain, amount: Double, gravel: List[Pit], timestamp: Date) {
  override def toString: String = s"Ordered: grain: $grain amount: $amount\n" +
    s"Fulfillment: [\n${gravel.map(_.toString).mkString("\n")}\n]"
}

/** Factory for [[gravel.model.Order]] instances. */
object Order {
  /** Creates a Order with a given grain, amount and order fulfilling pits.
    *
    * Automatically adds timestamp of creation.
    *
    * @param grain  the interval of ordered gravel grain
    * @param amount the amount of ordered gravel
    * @param gravel the list of gravel pits fulfilling the order
    */
  def apply(grain: Grain, amount: Double, gravel: List[Pit]): Order =
    new Order(grain, amount, gravel, Calendar.getInstance().getTime)
}
