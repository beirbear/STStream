package STStream.Generals

import java.nio.file.{Files, Paths}

/**
  * Created by beir on 1/17/17.
  */
object SecureKey {

  def isSecureKeyValid(input: String): Boolean = {
    if (!Services.isFileFolderExist(STStream.Definitions.getSecureKeyPath))
      throw new Exception("Security key doesn't exist!")

    if (input.isEmpty)
      return false

    val tmp = Services.valueHexOf(Files.readAllBytes(Paths.get(STStream.Definitions.getSecureKeyPath)).toList)

    if (tmp == input) true else false
  }

  def isSettingKeyValid(input: String): Boolean = {
    if (!Services.isFileFolderExist(STStream.Definitions.getSettingKeyPath))
      throw new Exception("Setting key doesn't exist!")

    if (input.isEmpty)
      return false

    val tmp = Services.valueHexOf(Files.readAllBytes(Paths.get(STStream.Definitions.getSettingKeyPath)).toList)

    if (tmp == input) true else false
  }

  def destroySecureKey(): Unit = {
    // Remove daily key content
    import java.io._
    val file = new File(STStream.Definitions.getSecureKeyPath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("")
    bw.close()
    Logger.warn("Security: Secure Key has been destroyed!")
  }

  def destroySettingKey(): Unit = {
    // Remove setting key content
    import java.io._
    val file = new File(STStream.Definitions.getSettingKeyPath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("")
    bw.close()
    Logger.warn("Security: Setting Key has been destroyed!")
  }

  def readSecureKey: Unit = {
    val byteArray = Services.valueHexOf(Files.readAllBytes(Paths.get(STStream.Definitions.getSecureKeyPath)).toList)
    println("Secure Key")
    println(byteArray)
  }

  def readSettingKey: Unit = {
    val byteArray = Services.valueHexOf(Files.readAllBytes(Paths.get(STStream.Definitions.getSettingKeyPath)).toList)
    println("Setting Key")
    println(byteArray)
  }
}
