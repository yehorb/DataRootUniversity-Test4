package Repositories

import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

package object implicits {
  implicit val localDateTimeToJavaSqlTimestampMapper: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] =
    MappedColumnType.base[java.time.LocalDateTime, java.sql.Timestamp](java.sql.Timestamp.valueOf, _.toLocalDateTime)
}
