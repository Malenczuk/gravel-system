package gravel.model

/** A Grain interval */
sealed case class Grain private(min: Double, max: Double) {
  def size: Double = max - min

  override def toString: String = s"($min, $max]"
}

/** Factory for [[gravel.model.Grain]] instances. */
object Grain {
  def apply(min: Double, max: Double): Grain = new Grain(math.min(min, max), math.max(min, max))
}