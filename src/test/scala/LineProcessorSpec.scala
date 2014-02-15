import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import scala.concurrent.duration._

class LineProcessorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("remove-duplicates"))

  override def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "A LineProcessor Actor" should "be able to store a line" in {
    val testLine = "The quick brown fox"
    val lineProcessor = TestActorRef(LineProcessor.props)
    lineProcessor ! ProcessLine(testLine)
    val lines = lineProcessor.underlyingActor.asInstanceOf[LineProcessor].lines
    lines should contain(testLine)
    lines.size should be (1)
  }

}
