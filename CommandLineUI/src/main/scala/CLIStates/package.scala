import org.rogach.scallop.ScallopOption

package object CLIStates {
  implicit class scallopOptionConversions[T](scallopOption: ScallopOption[T]) {
    def getValue: T = scallopOption.toOption.get
  }
}
