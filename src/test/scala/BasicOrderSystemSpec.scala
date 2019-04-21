import gravel.model._
import gravel.system.OrderSystem
import gravel.system.impl.BasicOrderSystem
import org.scalatest.{BeforeAndAfter, EitherValues, FlatSpec}

class BasicOrderSystemSpec extends FlatSpec with BeforeAndAfter with EitherValues {
  var system: OrderSystem = _

  before {
    system = new BasicOrderSystem
  }

  "A Order System" should "return placed order" in {
    val order = system.placeOrder(Grain(0, 1), 1, List(Pit(Grain(0, 1), 1)))
    assert(order.amount == 1)
    assert(order.grain == Grain(0, 1))
    assert(order.gravel == List(Pit(Grain(0, 1), 1)))
  }

  "A Order System" should "return list of postedOrders" in {
    assert(system.getOrders == List())
    val order = system.placeOrder(Grain(0, 1), 1, List(Pit(Grain(0, 1), 1)))
    assert(system.getOrders == List(order))
  }
}
