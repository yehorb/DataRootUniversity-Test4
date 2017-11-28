package CLIStates

import UIState.UIState
import Model.User
import org.rogach.scallop.{ScallopConf, ScallopOption}

class UnauthorizedUser extends UIState {
  override val name: String = "Unauthorized User. Type 'help' to view available commands. Type 'exit' to exit."

  override def help(): (UIState, String) = (
    this,
    """user new <username> <password>
      |         - create new user with provided username and password.
      |user login <username> <password>
      |         - login to existing account.""".stripMargin
  )

  override val operations: Map[String, Map[String, String => (UIState, String)]] = Map(
    "user" -> Map(
      "new"   -> ( (args) => userNew(args) ),
      "login" -> ( (args) => userLogin(args) )
    )
  )

  class userArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val username: ScallopOption[String] = trailArg[String](required = true)
    val password: ScallopOption[String] = trailArg[String](required = true)
    verify()
  }

  def userNew(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val userData = new userArgs(args)
      if (Operations.createNewUser(User(None, userData.username.getValue, userData.password.getValue)))
        (this, s"User ${userData.username.getValue} successfully created.")
      else
        (this, s"Username ${userData.username.getValue} already exists.")
    } catch {
      case _: Throwable =>
        (this, "Please supply valid username and password.")
    }
  }

  def userLogin(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val userData = new userArgs(args)
      val maybeUser = Operations.login(User(None, userData.username.getValue, userData.password.getValue))
      if (maybeUser.isDefined)
        (new AuthorizedUser(maybeUser.get), "Login successful.")
      else
      (this, "Invalid username or password.")
    } catch {
      case _: Throwable =>
        (this, "Please supply valid username and password.")
    }
  }

}
