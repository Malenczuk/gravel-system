import cats.effect.{ExitCode, IO, IOApp}
import gravel.model.Grain
import gravel.system.GravelPitSystem
import gravel.system.impl.{BasicGravelPitSystem, BasicOrderSystem}

import scala.util.matching.Regex

object Application extends IOApp {
  val produceCmd: Regex = """\s*produce\s+(\d*\.?\d+)\s+(\d*\.?\d+)\s+(\d*\.?\d+)\s*""".r
  val orderCmd: Regex = """\s*order\s+(\d*\.?\d+)\s+(\d*\.?\d+)\s+(\d*\.?\d+)\s*""".r
  val deleteCmd: Regex = """\s*delete\s+(\d*\.?\d+)\s+(\d*\.?\d+)\s*""".r
  val statusCmd: Regex = """\s*status\s*""".r
  val ordersCmd: Regex = """\s*orders\s*""".r


  def loop(system: GravelPitSystem): IO[ExitCode] = for {
    input <- IO(scala.io.StdIn.readLine("> "))
    _ <- input match {
      case produceCmd(minGrain, maxGrain, amount) =>
        system.production(Grain(minGrain.toDouble, maxGrain.toDouble), amount.toDouble) match {
          case Left(error) => IO(println(error))
          case Right(pits) => IO(pits.foreach(println))
        }
      case orderCmd(minGrain, maxGrain, amount) =>
        system.order(Grain(minGrain.toDouble, maxGrain.toDouble), amount.toDouble) match {
          case Left(error) => IO(println(error))
          case Right(order) => IO(println(order))
        }
      case deleteCmd(minGrain, maxGrain) =>
        system.delete(Grain(minGrain.toDouble, maxGrain.toDouble)) match {
          case Left(error) => IO(println(error))
          case Right(pit) => IO(println(pit))
        }
      case statusCmd() =>
        IO(system.status().foreach(println))
      case ordersCmd() =>
        IO(system.orderSystem.getOrders.foreach(println))
      case _ =>
        IO(println("produce [minGrain] [maxGrain] [amount]\n" +
          "order [minGrain] [maxGrain] [amount]\n" +
          "delete [minGrain] [maxGrain]\n" +
          "status\n" +
          "orders"))
    }
    _ <- loop(system)
  } yield ExitCode.Success


  def run(args: List[String]): IO[ExitCode] = {
    val system = new BasicGravelPitSystem(new BasicOrderSystem())
    loop(system)
  }
}
