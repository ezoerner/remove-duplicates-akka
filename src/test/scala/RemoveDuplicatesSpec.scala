import java.io.{PrintWriter, File}
import org.scalatest._
import scala.collection.mutable
import scala.util.Random
import resource._
import scala.io.Source

/**
 * // TODO: Add class description here.
 *
 * @author Eric Zoerner <a href="mailto:eric.zoerner@gmail.com">eric.zoerner@gmail.com</a>
 */
class RemoveDuplicatesSpec extends FlatSpec with Matchers {

  def withFile(numLines: Int, maxLineLength: Int)(testCode: (File, File) => Any) {
    val inFile = File.createTempFile("remove-dup-in", null)
    val outFile = File.createTempFile("remove-dup-out", null)
    generate(inFile, numLines, maxLineLength)
    testCode(inFile, outFile)
  }

  def generate(file: File, numLines: Int, maxLineLength: Int) = {
    assert(maxLineLength >= 21)
    var numDups = 0
    val builder = new StringBuilder()
    for (out <- managed(new PrintWriter(file));
         _ <- 1 to numLines;
          // use at least a line length of 20 to help make sure lines are unique
         lineLength = Random.nextInt(maxLineLength - 19) + 20;
         col <- 1 to lineLength) {
      builder.append(Random.nextPrintableChar())
      if (col == lineLength) {
        val string: String = builder.toString()
        out.println(string)
        // chance of duplicate
        while (Random.nextInt(6) == 0) {
          numDups += 1
          out.println(string)
        }
        builder.clear()
      }
    }

    assertResult(numLines + numDups,
                 s"numDups=$numDups;maxLineLength=$maxLineLength;file=$file.getPath") {
      countLines(file)
    }
  }

  def countLines(file: File): Int = Source.fromFile(file).getLines.size

  "RemoveDuplicates" should "produce correct output for small input file" in withFile(5, 21) { (inFile, outFile) =>
    Console.out.println("input path=" + inFile.getPath)
    Console.out.println("output path=" + outFile.getPath)
    RemoveDuplicates.main(Array(inFile.getPath, outFile.getPath))
    countLines(outFile) should be (5)
  }

  "RemoveDuplicates" should "produce correct output for larger input file" in withFile(3000, 120) { (inFile, outFile) =>
    Console.out.println("input path=" + inFile.getPath)
    Console.out.println("output path=" + outFile.getPath)
    RemoveDuplicates.main(Array(inFile.getPath, outFile.getPath))
    countLines(outFile) should be (3000)
  }
}
