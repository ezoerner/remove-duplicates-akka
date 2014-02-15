import java.io._
import scala.util.Random

val out = new PrintWriter("src/test/resources/test.txt")
System.out.println(new File("test.txt").getAbsolutePath)
val builder = new StringBuilder()
for (_ <- 1 to 3000;
     col <- 1 to Random.nextInt(128)) {
  builder.append(Random.nextPrintableChar())
  if (col == 1) {
    val string: String = builder.toString()
    out.println(string)
    // chance of duplicate
    while (Random.nextInt(6) == 1) {
      out.println(string)
    }
    builder.clear()
  }
}
out.close()

