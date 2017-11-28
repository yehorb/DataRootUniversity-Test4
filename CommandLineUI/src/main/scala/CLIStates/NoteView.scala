package CLIStates

import Model.Note
import UIState.UIState
import org.rogach.scallop.{ScallopConf, ScallopOption}

class NoteView(parent: AuthorizedUser, note: Note) extends UIState {
  override val name: String = s"Note [${note.noteId}] by [${parent.username}]"

  override def help(): (UIState, String) = (
    this,
    """view note -
      |update note - create new user with provided username and password.
      |delete note - delete current user and all of his notes.
      |go back - change password for current user.""".stripMargin
  )

  override val operations: Map[String, Map[String, String => (UIState, String)]] = Map(
    "view" -> Map(
      "note"   -> ( (_) => viewNote )
    ),
    "update" -> Map(
      "note"   -> ( (updateArgs) => update(updateArgs) )
    ),
    "delete" -> Map(
      "note"   -> ( (_) => delete )
    ),
    "go" -> Map(
      "back" -> ( (_) => (parent, "") )
    )
  )

  def viewNote: (UIState, String) = {
    val noteString =
      s"Status: ${if (note.done) "Done" else "In progress"}; Priority: ${note.priority}; Last edit: ${note.edited} \n\n" +
      note.header + "\n\n" +
      note.contents
    (this, noteString)
  }

  class editNoteArgs(args: Seq[String]) extends ScallopConf(args) {
    override def onError(e: Throwable): Unit =
      throw e
    val priority: ScallopOption[Int] = opt[Int](validate = (1 to 5).contains)
    val header: ScallopOption[List[String]] = opt[List[String]]()
    val contents: ScallopOption[List[String]] = opt[List[String]]()
    val done: ScallopOption[Boolean] = opt[Boolean]()
    verify()
  }
  def update(str: String): (UIState, String) = {
    val args = str.split(' ')
    try {
      val parsed = new editNoteArgs(args)
      val editedNote = note.copy(
        header = parsed.header.getOrElse(List(note.header)).mkString(" "),
        contents = parsed.contents.getOrElse(List(note.contents)).mkString(" "),
        priority = parsed.priority.getOrElse(note.priority),
        done = if (parsed.done.getOrElse(false)) !note.done else note.done,
      )
      if (Operations.updateNote(editedNote))
        (new NoteView(parent, editedNote), "Note successfully updated")
      else
        (this, "Something gone wrong")
    } catch {
      case _: Throwable =>
        (this, "Please provide valid trailing arguments")
    }
  }

  def delete: (UIState, String) = {
    if (Operations.deleteNote(note.noteId.get))
      (parent, "Note successfully deleted")
    else
      (this, "Something gone wrong")
  }
}
