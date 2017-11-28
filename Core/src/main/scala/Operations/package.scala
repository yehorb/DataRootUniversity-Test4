import CommonOperations.exec
import Model.{Note, User}
import Repositories.{NoteRepository, UserRepository}

package object Operations {
  val users: UserRepository = Repositories.getUsers
  val notes: NoteRepository = Repositories.getNotes

  def createNewUser(user: User): Boolean = {
    if (!exec(users.usernameExists(user.username))) {
      exec(users.create(user))
      true
    }
    else
      false
  }

  def login(user: User): Option[User] = {
    exec(users.readUser(user))
  }

  def updateUser(user: User): Boolean = {
    try {
      exec(users.update(user))
      true
    } catch {
      case ex: Exception =>
        println(ex.getMessage)
        false
    }
  }

  def deleteUser(user: User): Boolean = {
    try {
      exec(users.delete(user.userId.get))
      true
    } catch {
      case ex: Exception =>
        println(ex.getMessage)
        false
    }
  }

  def listNotes(user: User): Seq[Note] = {
    exec(notes.readAll(user.userId.get))
  }

  def readNote(noteId: Long): Option[Note] = {
    exec(notes.readById(noteId))
  }

  def addNote(note: Note): Boolean = {
    try {
      exec(notes.create(note))
      true
    } catch {
      case ex: Exception =>
        println(ex.getMessage)
        false
    }
  }

  def deleteNote(noteId: Long): Boolean = {
    try {
      exec(notes.delete(noteId))
      true
    } catch {
      case ex: Exception =>
        println(ex.getMessage)
        false
    }
  }

  def updateNote(note: Note): Boolean = {
    try {
      exec(notes.update(note))
      true
    } catch {
      case ex: Exception =>
        println(ex.getMessage)
        false
    }
  }

  def recreateSchema(): Unit = {
    exec(Repositories.db.run(Repositories.dropSchema))
    exec(Repositories.db.run(Repositories.createSchemaQuery))
  }

}
