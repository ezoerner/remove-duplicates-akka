import java.io._
import scala.util.Random

val out = new PrintWriter("src/test/resources/test.txt")
val builder = new StringBuilder()
for (_ <- 1 to 3000;
     // use at least a line length of 20 to help make sure lines are unique
     lineLength = Random.nextInt(128 - 19) + 20;
     col <- 1 to lineLength) {
  builder.append(Random.nextPrintableChar())
  if (col == lineLength) {
    val string: String = builder.toString()
    out.println(string)
    // chance of duplicate
    while (Random.nextInt(6) == 0) {
      out.println(string)
    }
    builder.clear()
  }
}
out.close()

