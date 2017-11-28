import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object CommonOperations {
  def exec[T](future: Future[T]): T =
    Await.result(future, Duration.Inf)
}
