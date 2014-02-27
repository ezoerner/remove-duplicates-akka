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
  val startTime = System.currentTimeMillis()
  var firstLineOutput = false

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

    case LineProcessed(line: Option[String]) =>
      awaitingCount -= 1
      if (line.isDefined) {
        out.println(line.get)
        if (!firstLineOutput) {
          firstLineOutput = true
          Console.out.println("TIME: " + (System.currentTimeMillis() - startTime))
        }
      }

      if (awaitingCount <= 0) {
        Console.out.println(s"Finished processing $lineCount lines.")
        out.close()
        context.stop(self)
      }
  }
}