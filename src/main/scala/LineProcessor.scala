import akka.actor.{Actor, Props}
import scala.collection.mutable

/**
 * Actor to process lines.
 *
 * @author Eric Zoerner <a href="mailto:eric.zoerner@gmail.com">eric.zoerner@gmail.com</a>
 */
object LineProcessor {
  def props: Props = Props(new LineProcessor)
}

class LineProcessor extends Actor {
  val lines: mutable.Set[String] = mutable.Set[String]()

  def receive = {
    case ProcessLine(line: String) => processLine(line: String)
  }

  private def processLine(line: String) =
    if (lines add line)
      sender ! LineProcessed(Some(line))
    else
      sender ! LineProcessed(None)
}
