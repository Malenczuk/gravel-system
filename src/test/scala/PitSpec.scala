import gravel.error._
import gravel.model._
import org.scalatest.{BeforeAndAfter, EitherValues, FlatSpec}

class PitSpec extends FlatSpec with BeforeAndAfter with EitherValues {

  var pit: Pit = _

  before {
    pit = Pit(Grain(1, 2), 10)
  }

  "A Pit" should "return InsufficientGravelAmount Error if taking more than it is on the pit" in {
    assert(pit.take(amount = 20).left.value == InsufficientGravelAmount(-10))
  }

  "A Pit" should "return a Pit with reduced amount when taking form the pit" in {
    assert(pit.take(amount = 5).right.value == Pit(Grain(1, 2), 5))
  }

  "A Pit" should "return a Pit with increased amount when putting to the pit" in {
    assert(pit.put(amount = 5) == Pit(Grain(1, 2), 15))
  }

  "A Pit" should "return a Pit with increased or unchanged granulation when updating the pit" in {
    assert(pit.updateGranulation(Grain(1.5, 1.75)) == pit)
    assert(pit.updateGranulation(Grain(.5, 1.75)) == Pit(Grain(.5, 2), 10))
    assert(pit.updateGranulation(Grain(1.5, 2.75)) == Pit(Grain(1, 2.75), 10))
    assert(pit.updateGranulation(Grain(.5, 2.75)) == Pit(Grain(.5, 2.75), 10))
  }

  "A Pit" should "return True if Pit contains grain range" in {
    assert(pit.contains(Grain(1.25, 1.75)))
  }

  "A Pit" should "return false if Pit does not contains grain range" in {
    assert(!pit.contains(Grain(.5, 2.5)))
  }

  "A Pit" should "return True if Pit intersects with grain range" in {
    assert(pit.intersects(Grain(.5, 1.5)))
  }

  "A Pit" should "return false if Pit does not intersect with grain range" in {
    assert(!pit.contains(Grain(.5, .75)))
  }
}
