import slick.jdbc.PostgresProfile.api._

package object Repositories {
//  val db = Database.forURL(
//    "jdbc:postgresql://ec2-174-129-4-27.compute-1.amazonaws.com:5432/d1pf1fe8roqbcp?user=izdsdyginkuhel&password=d6bf318b0b9ae8c1a3189adaad3bd9756d3c8bc9a6da8c9f58343af217d2844d&sslmode=require",
//    driver="org.postgresql.Driver"
//  )
  val db = Database.forConfig("todo-list-local")

  val createSchemaQuery = Users.createQuery >> Notes.createQuery
  val truncateSchemaQuery = Notes.truncateQuery >> Users.truncateQuery
  val dropSchema = Notes.dropQuery >> Users.dropQuery

  def getUsers = new UserRepository(db)
  def getNotes = new NoteRepository(db)
}
