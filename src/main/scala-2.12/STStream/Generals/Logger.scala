package STStream.Generals

/**
  * Created by beir on 2/6/17.
  */
object Logger {

  def write(input: String): Unit = {
    println("OUT: " + input)
  }

  def warn(input: String): Unit = {
    println("WRN: " + input)
  }

  def error(input: String): Unit = {
    println("ERR: " + input)
  }

  def errorExit(input: String, errCode: Int): Unit = {
    println("ERR-EXT: " + input)
    System.exit(errCode)
  }
}
