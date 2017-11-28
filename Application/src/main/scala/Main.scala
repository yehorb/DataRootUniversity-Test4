import CLIStates.UnauthorizedUser
import UIState.UIState

import scala.util.matching.Regex

object Main {
  val noArgs: Regex = """([a-z]+) ([a-z]+)""".r
  val withArgs: Regex = """([a-z]+) ([a-z]+) (.+)""".r

  def loop(uiState: UIState, response: String): Unit = {
    println(response + '\n')
    println(uiState.name + '\n')
    val userInput = scala.io.StdIn.readLine()
     userInput match {
       case noArgs(task, subtask) =>
         val nextAction = uiState.run(task, subtask, "")
         loop(nextAction._1, nextAction._2)

       case withArgs(task, subtask, args) =>
        val nextAction = uiState.run(task, subtask, args)
        loop(nextAction._1, nextAction._2)

      case "help" =>
        val help = uiState.help()
        loop(help._1, help._2)

      case "exit" =>
        uiState.exit()

      case _ =>
        loop(uiState, "No such command")
    }
  }

  def main(args: Array[String]): Unit = {
    loop(new UnauthorizedUser, "")
  }

}
