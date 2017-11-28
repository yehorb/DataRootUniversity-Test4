import org.rogach.scallop.ScallopOption

package object CLIStates {
  implicit def scallopOptionToValue[T](scallopOption: ScallopOption[T]): T =
    scallopOption.toOption.get
}
