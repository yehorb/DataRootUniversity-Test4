package Repositories

import Model._
import slick.jdbc.PostgresProfile.api._
import implicits._

import scala.concurrent.Future

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def userId   = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def password = column[String]("password")

  def * = (userId.?, username, password).mapTo[User]
}

object Users {
  val table = TableQuery[UserTable]

  val createQuery = table.schema.create
  val truncateQuery = table.schema.truncate
  val dropQuery = table.schema.drop
}

class UserRepository(db: Database) {
  val userTableQuery = Users.table

  def create(user: User): Future[User] =
    db.run(userTableQuery returning userTableQuery += user)

  def readExists(username: String): Future[Boolean] =
    db.run(userTableQuery.filter(_.username === username).exists.result)

  def readByName(username: String): Future[Option[User]] =
    db.run(userTableQuery.filter(_.username === username).result.headOption)

  def update(user: User): Future[Int] =
    db.run(userTableQuery.filter(_.userId === user.userId).update(user))

  def delete(userId: Long): Future[Int] =
    db.run(userTableQuery.filter(_.userId === userId).delete)
}
