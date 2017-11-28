package Repositories

import Model._
import slick.jdbc.PostgresProfile.api._
import implicits._

import java.time.LocalDateTime
import scala.concurrent.Future

class NoteTable(tag: Tag) extends Table[Note](tag, "Note") {
  def noteId   = column[Long]("note_id", O.AutoInc, O.PrimaryKey)
  def userId   = column[Long]("user_id")
  def header   = column[String]("header")
  def contents = column[String]("contents")
  def edited   = column[LocalDateTime]("edited")
  def priority = column[Int]("priority")
  def done     = column[Boolean]("done")

  def userIdFk = foreignKey("user_id_fk", userId, TableQuery[UserTable])(_.userId, onDelete = ForeignKeyAction.Cascade)

  def * = (noteId.?, userId, header, contents, edited, priority, done).mapTo[Note]
}

object Notes {
  val table = TableQuery[NoteTable]

  val createQuery = table.schema.create
  val truncateQuery = table.schema.truncate
  val dropQuery = table.schema.drop
}

class NoteRepository(db: Database) {
  val noteTableQuery = Notes.table

  def create(note: Note): Future[Note] =
    db.run(noteTableQuery returning noteTableQuery += note)

  def readAll(userId: Long): Future[Seq[Note]] =
    db.run(noteTableQuery.filter(_.userId === userId).sortBy( note => (note.done.asc, note.priority.desc, note.edited.desc) ).result)

  def readById(noteId: Long): Future[Option[Note]] =
    db.run(noteTableQuery.filter(_.noteId === noteId).result.headOption)

  def update(note: Note): Future[Int] =
  db.run(noteTableQuery.filter(_.noteId === note.noteId).update(note))

  def delete(noteId: Long): Future[Int] =
  db.run(noteTableQuery.filter(_.noteId === noteId).delete)
}
