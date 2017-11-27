import java.time.LocalDateTime

package object Model {
  case class User(userId: Option[Long],
                  username: String,
                  password: String)
  case class Note(noteId: Option[Long],
                  header: String,
                  contents: String,
                  created: LocalDateTime,
                  edited: LocalDateTime,
                  priority: Int, done: Boolean)
}
