import Model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.matching.Regex

sealed trait State {
  val name: String
  val operations: Map[String, Map[String, (String) => (State, String)]]

  def run(operation: String, subOperation: String, args: String): (State, String) =
    for {
      operation <- operations.get(operation)
      subOperation <- operation.get(subOperation)
      result <- subOperation(args)
    } yield result match {
      case Some((state, resultString)) => (state, resultString)
      case _ => (this, "No such operation")
    }

  def exit(): (State, String) = {
    System.exit(0)
    (this, "")
  }

  def exec[T](future: Future[T]): T =
    Await.result(future, Duration.Inf)
}

class UnauthorizedUser extends State {
  override val name: String = "Unauthorized User"
  override val operations: Map[String, Map[String, String => (State, String)]] =
    Map(
      "help" -> Map( "" -> ((_) => (this, "Help for UU")) ),
      "user" -> Map(
        "new" -> ((userCredentials) => createNewUser(userCredentials)),
        "login" -> ((userCredentials) => login(userCredentials))
      ),
      "exit" -> Map( "" -> ((_) => exit() ))
    )

  def createNewUser(str: String): (State, String) = {
    val userData = """([A-za-z0-9.]+) ([\d\D]+)""".r
    str match {
      case userData(username, password) =>
        val users = Repositories.getUsers
        val newUser = User(None, username, password)
        exec(users.create(newUser))
        (this, s"User ${newUser.username} successfully created")
      case _ =>
        (this, s"[$str] are not valid username and password")
    }
  }

  def login(str: String): (State, String) = {
    val userData = """([A-za-z0-9.]+) ([\d\D]+)""".r
    str match {
      case userData(username, password) =>
        val users = Repositories.getUsers
        val newUser = User(None, username, password)
        val realUser = exec(users.readByName(username))
        if (realUser.contains(newUser))
          (new AuthorizedUser(newUser.username), s"User ${newUser.username} login successful")
        else
          (this, s"[$str] are not valid username and password")
      case _ =>
        (this, s"[$str] are not valid username and password")
    }
  }
}

class AuthorizedUser(username: String) extends State {
  override val name: String = s"Authorized User: $username"
  override val operations: Map[String, Map[String, String => (State, String)]] = Map(
    "help" -> Map( "" -> ((_) => (this, "Help for AU")) )
  )
}
