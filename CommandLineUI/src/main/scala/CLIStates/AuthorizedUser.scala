package CLIStates

import UIState.UIState
import Model.{Note, User}
import org.rogach.scallop.{ScallopConf, ScallopOption}

class AuthorizedUser(user: User) extends UIState {
  val username = s"Authorized User ${user.username}"
  override val name: String = s"$username. Type 'help' to view available commands. Type 'exit' to exit."

  override def help(): (UIState, String) = (
    this,
    """user logout
      |         - self explanatory.
      |user delete
      |         - delete current user and all of his notes.
      |user password <old_password> <new_password>
      |         - change password for current user.
      |
      |notes list
      |         - list of all the notes by this user in format | note_id | is_done | priority | last_edited | header | contents |.
      |notes view <note_id>
      |         - view specified note in details.
      |note new -p|--priority <note_priority> -h|--header <note_header> -c|--contents <note_contents>
      |         - create new note for current user. Priority is number from 1 to 5.
      |note update -n|--noteId <note_id> [-p|--priority <note_priority>] [-h|--header <note_header>] [-c|--contents <note_contents>] [-d|--done]
      |         - update specified note with optional data. Priority is number from 1 to 5. -d|--done flips "done" flag (e.g false -> true).
      |note delete -n|--noteId <note_id>
      |         - deletes specified note.""".stripMargin
  )

  override val operations: Map[String, Map[String, String => (UIState, String)]] = Map(
    "user" -> Map(
      "logout"   -> ( (_) => (new UnauthorizedUser, "Logged out successful") ),
      "delete"   -> ( (_) => userDelete(user) ),
      "password" -> ( (passwords) => userPassword(passwords) )
    ),
    "notes" -> Map(
      "list"   -> ( (_)            => notesList ),
      "new"    -> ( (noteContents) => notesNew(noteContents) ),
      "update" -> ( (updateArgs)   => notesUpdate(updateArgs) ),
      "delete" -> ( (noteId)       => noteDelete(noteId) ),
      "view"   -> ( (noteId)       => viewNote(noteId) )
    )
  )

  def userDelete(user: User): (UIState, String) = {
    if (Operations.deleteUser(user))
      (new UnauthorizedUser, s"User ${user.username} deleted successfully.")
    else
      (this, s"Unable to delete user ${user.username}")
  }

  class passwordArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val oldPassword: ScallopOption[String] = trailArg[String](required = true)
    val newPassword: ScallopOption[String] = trailArg[String](required = true)
    verify()
  }
  def userPassword(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val passData = new passwordArgs(args)
      if (passData.oldPassword.toOption.get == user.password) {
        val newUser = user.copy(password = passData.newPassword.getValue)
        if (Operations.updateUser(newUser))
          (new AuthorizedUser(newUser), "Password changed successfully")
        else
          (this, "Password change failed")
      } else {
        (this, "Invalid old password, try again")
      }
    } catch {
      case _: Throwable =>
        (this, "Please supply valid old password and new password.")
    }
  }

  def notesList: (UIState, String) = {
    val notes = Operations.listNotes(user)
    if (notes.isEmpty)
      (this, "This user has no notes")
    else {
      def ellipsize(str: String, len: Int): String =
        if (str.length < len)
          str.padTo(len, ' ')
        else if (str.length == len)
          str
        else
          str.take(len - 3).concat("...")

      val noteString =
        notes.
          map( note => f"| ${note.noteId.get}%3d | ${if (note.done) '+' else  '-'} | ${note.priority} | ${note.edited.toLocalDate} | ${ellipsize(note.header, 15)} | ${ellipsize(note.contents, 30)} |" ).
          mkString("\n")
      (this, noteString)
    }
  }

  class newNoteArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val priority: ScallopOption[Int] = opt[Int](required = true, validate = (1 to 5).contains )
    val header: ScallopOption[List[String]] = opt[List[String]](required = true)
    val contents: ScallopOption[List[String]] = opt[List[String]](required = true)
    verify()
  }
  def notesNew(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val parsed = new newNoteArgs(args)
      val newNote = Note(
        None,
        user.userId.get,
        parsed.header.getValue.mkString(" "),
        parsed.contents.getValue.mkString(" "),
        java.time.LocalDateTime.now(),
        parsed.priority.getValue,
        done = false
      )
      if (Operations.addNote(newNote))
        (this, "New note successfully created")
      else
        (this, "Note creation failed")
    } catch {
      case _: Throwable =>
        (this, "Provide valid priority, header and contents")
    }
  }

  class editNoteArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val noteId: ScallopOption[Long] = opt[Long](required = true)
    val priority: ScallopOption[Int] = opt[Int](validate = (1 to 5).contains)
    val header: ScallopOption[List[String]] = opt[List[String]]()
    val contents: ScallopOption[List[String]] = opt[List[String]]()
    val done: ScallopOption[Boolean] = opt[Boolean]()
    verify()
  }
  def notesUpdate(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val parsed = new editNoteArgs(args)
      val maybeNote = Operations.readNote(parsed.noteId.getValue)
      if (maybeNote.isDefined) {
        val realNote = maybeNote.get
        val editedNote = realNote.copy(
          header = parsed.header.getOrElse(List(realNote.header)).mkString(" "),
          contents = parsed.contents.getOrElse(List(realNote.contents)).mkString(" "),
          priority = parsed.priority.getOrElse(realNote.priority),
          done = if (parsed.done.getOrElse(false)) !realNote.done else realNote.done,
          edited = java.time.LocalDateTime.now()
        )
        if (Operations.updateNote(editedNote))
          (this, "Note successfully updated")
        else
          (this, "Something gone wrong")
      } else
        (this, "No note with such ID")
    } catch {
      case _: Throwable =>
        (this, "Please provide valid note ID and trailing arguments")
    }
  }

  class noteIdArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val noteId: ScallopOption[Long] = trailArg[Long](required = true)
    verify()
  }

  def noteDelete(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val parsed = new noteIdArgs(args)
      if (Operations.deleteNote(parsed.noteId.getValue))
        (this, "Note successfully deleted")
      else
        (this, "Something gone wrong")
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        (this, "Please provide valid note ID")
    }
  }

  def viewNote(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val parsed = new noteIdArgs(args)
      val maybeNote = Operations.readNote(parsed.noteId.getValue)
      if (maybeNote.isDefined) {
        val realNote = maybeNote.get
        (new NoteView(this, realNote), "")
      } else
        (this, "No note with such ID")
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        (this, "Please provide valid note ID")
    }
  }

}
