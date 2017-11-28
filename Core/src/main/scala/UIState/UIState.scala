package UIState

trait UIState {
  val name: String
  val operations: Map[String, Map[String, (String) => (UIState, String)]]

  def help(): (UIState, String)

  def run(operation: String, subOperation: String, args: String): (UIState, String) = {
    val maybeResult = for {
      operation <- operations.get(operation)
      subOperation <- operation.get(subOperation)
    } yield subOperation
    maybeResult.getOrElse((arg: Any) => (this, "No such operation"))(args)
  }

  def exit(): Unit = {
    Repositories.db.close()
    System.exit(0)
  }
}
