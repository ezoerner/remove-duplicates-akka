import akka.actor.{Actor, Props}
import akka.routing.{ConsistentHashingRouter, RouterConfig, Broadcast, FromConfig}
import com.typesafe.config.ConfigFactory
import java.io.PrintWriter
import resource._
import scala.io.Source

/**
 * Actor to process files.
 *
 * @author Eric Zoerner <a href="mailto:eric.zoerner@gmail.com">eric.zoerner@gmail.com</a>
 */
object FileProcessor {
  def props: Props = Props(new FileProcessor)
}

class FileProcessor extends Actor {
  var out: PrintWriter = null
  var awaitingCount: Int = 0
  var lineCount: Int = 0
  val router = context.actorOf(LineProcessor.props.withRouter(FromConfig()), "lineProcessorRouter")
  val numChildren = ConfigFactory.load().getInt("akka.actor.deployment./fileProcessor/lineProcessorRouter.nr-of-instances")
  var numChildrenDone = 0

  def processFile(inFilename: String) = {
    for (file1 <- managed(Source.fromFile(inFilename));
         line <- file1.getLines()) {
      awaitingCount += 1
      lineCount += 1
      router ! ProcessLine(line)
    }
  }

  def receive = {
    case ProcessFile(inFilename: String, outFilename: String) =>
      processFile(inFilename: String)
      out = new PrintWriter(outFilename)
    case LineProcessed =>
      awaitingCount -= 1
      if (awaitingCount <= 0) {
        Console.out.println(s"Finished processing $lineCount lines.")
        Console.out.println("Requesting results..")
        router ! Broadcast(GetLines)
      }
    case ProcessLine(line) =>
      out.println(line)
    case Done =>
      numChildrenDone += 1
      if (numChildrenDone >= numChildren) {
        out.close()
        context.stop(self)
      }
  }
}