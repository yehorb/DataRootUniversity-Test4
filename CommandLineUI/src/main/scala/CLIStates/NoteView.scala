package CLIStates

import Model.Note
import UIState.UIState
import org.rogach.scallop.{ScallopConf, ScallopOption}

class NoteView(parent: AuthorizedUser, note: Note) extends UIState {
  override val name: String = s"Note [${note.noteId.get}] by [${parent.username}]. Type 'help' to view available commands. Type 'exit' to exit."

  override def help(): (UIState, String) = (
    this,
    """view note
      |         - print all of the note contents.
      |update note [-p|--priority <note_priority>] [-h|--header <note_header>] [-c|--contents <note_contents>] [-d|--done]
      |         - update specified note with optional data. Priority is number from 1 to 5. -d|--done flips "done" flag (e.g false -> true).
      |delete note
      |         - deletes this note.
      |go back
      |         - go back to Authorized User menu.""".stripMargin
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
        edited = java.time.LocalDateTime.now()
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
