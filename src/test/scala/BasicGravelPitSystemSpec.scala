import gravel.error._
import gravel.model._
import gravel.system.impl.{BasicGravelPitSystem, BasicOrderSystem}
import org.scalatest.{BeforeAndAfter, EitherValues, FlatSpec}

class BasicGravelPitSystemSpec extends FlatSpec with EitherValues with BeforeAndAfter {
  var system: BasicGravelPitSystem = _

  before {
    system = new BasicGravelPitSystem(new BasicOrderSystem)
  }

  "A Gravel Pit System" should "return NotAValidInterval Error if wrong range in production" in {
    assert(system.production(Grain(1, 1), 0).left.value == NotAValidInterval)
  }

  "A Gravel Pit System" should "return NegativeGrain Error if negative grain in production" in {
    assert(system.production(Grain(-1, 0), 0).left.value == NegativeGrain)
  }

  "A Gravel Pit System" should "return NegativeGravelAmount Error if negative amount in production" in {
    assert(system.production(Grain(0, 1), -1).left.value == NegativeGravelAmount)
  }

  "A Gravel Pit System" should
    "return NoValidGravelPit Error if there is no pit or can not crete a pit with selected grain interval" in {
    system.production(Grain(0, 1), 1)
    system.production(Grain(1, 2), 1)
    assert(system.production(Grain(.5, 1.5), 1).left.value == NoValidGravelPit)
  }


  "A Gravel Pit System" should "return all pits when producing" in {
    assert(system.production(Grain(0, 1), 1).right.value == List(Pit(Grain(0, 1), 1)))
    assert(system.production(Grain(1, 2), 1).right.value == List(Pit(Grain(0, 1), 1), Pit(Grain(1, 2), 1)))
    assert(system.production(Grain(0, 1), 3).right.value == List(Pit(Grain(0, 1), 4), Pit(Grain(1, 2), 1)))
  }

  "A Gravel Pit System" should "return pit when deleting" in {
    system.production(Grain(0, 1), 1)
    assert(system.delete(Grain(0, 1)).right.value == Pit(Grain(0, 1), 1))
  }

  "A Gravel Pit System" should "return NonExistingGravelPit Error when deleting" in {
    assert(system.delete(Grain(0, 1)).left.value == NonExistingGravelPit)
  }

  "A Gravel Pit System" should "return InsufficientGravelAmount Error if no gravel to finalize order" in {
    assert(system.order(Grain(0, 1), 1).left.value == InsufficientGravelAmount(-1))
  }

  "A Gravel Pit System" should "return finalized order if there is sufficient gravel amount" in {
    system.production(Grain(0, 1), 2)
    val order = system.order(Grain(0, 1), 1).right.value
    assert(order.amount == 1)
    assert(order.grain == Grain(0, 1))
    assert(order.gravel == List(Pit(Grain(0, 1), 1)))
    assert(system.status() == List(Pit(Grain(0, 1), 1)))
  }

  "A Gravel Pit System" should "return current status of pits" in {
    system.production(Grain(0, 1), 1)
    assert(system.status() == List(Pit(Grain(0, 1), 1)))
  }
}
