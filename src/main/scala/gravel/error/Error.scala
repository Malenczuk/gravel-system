package gravel.error

/** System wide error */
sealed abstract class Error {
  override def toString: String = "Gravel System Error occurred"
}

final case object NotAValidInterval extends Error {
  override def toString: String = "Given grain interval is invalid"
}

final case object NegativeGrain extends Error {
  override def toString: String = "Given grain interval has negative values"
}

final case object NegativeGravelAmount extends Error {
  override def toString: String = "Given Grain amount is negative"
}

final case object NoValidGravelPit extends Error {
  override def toString: String = "No valid gravel pit available for current operation"
}

final case object NonExistingGravelPit extends Error {
  override def toString: String = "Given gravel pit is non existent"
}

final case class InsufficientGravelAmount(missing: Double) extends Error {
  override def toString: String = s"Missing $missing gravel for current operation"
}