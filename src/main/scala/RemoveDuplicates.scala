import akka.actor.{Inbox, ActorSystem}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import scala.concurrent.duration._

/**
 * Remove duplicate lines in large file.
 *
 * @author Eric Zoerner <a href="mailto:eric.zoerner@gmail.com">eric.zoerner@gmail.com</a>
 */

// Messages

// Process a file
case class ProcessFile(inFilename: String, outFilename: String)

// Process a line of text
case class ProcessLine(line: String) extends ConsistentHashable {
  override def consistentHashKey: Any = line
}

// Request the lines
case object GetLines

// Notify line has been processed
case object LineProcessed

// Notify done sending results
case object Done

object RemoveDuplicates extends App {

  if (args.length != 2) {
    Console.out.println("args: <inFilename> <outFilename>")
    System.exit(0)
  }

  val inFile = args(0)
  val outFile = args(1)
  val system = ActorSystem("remove-duplicates")
  val fileProcessor = system.actorOf(FileProcessor.props, "fileProcessor")

  val inbox = Inbox.create(system)
  inbox.send(fileProcessor, ProcessFile(inFile, outFile))

  // wait for Terminated message
  inbox.watch(fileProcessor)
  inbox.receive(10.minutes)
  Console.out.println("Finished.")
  system.shutdown()
}
