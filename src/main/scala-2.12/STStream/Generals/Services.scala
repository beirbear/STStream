package STStream.Generals

import java.io.File
import java.nio.file.Paths

/**
  * Created by beir on 2/6/17.
  */
object Services {

  def isFileFolderExist(input: String): Boolean = new java.io.File(input).exists

  def valueHexOf(bytes : List[Byte]): String = bytes.map{ b => String.format("%02X", new java.lang.Integer(b & 0xff)) }.mkString

  def getFileNameWithoutExtension(input: String): String = getFileName(input).replaceFirst("[.][^.]+$", "")

  def getFilesInFolder(input: String): List[File] = {
    val d = new File(input)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).toList
    else
      List[File]()
  }

  def getFoldersInFolder(input: String): List[File] = {
    val d = new File(input)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isDirectory).toList
    else
      List[File]()
  }

  def getFileName(input: String): String = {
    val p = Paths.get(input)
    p.getFileName.toString
  }

  def zipFolder(input: String, output: String): Unit = {
    import sys.process._
    ("zip -r " + output + ".zip " + input).!
  }

  def deleteFolder(input: String): Unit = {
    def deleteRecursively(file: File): Unit = {
      if (file.isDirectory)
        file.listFiles.foreach(deleteRecursively)
      if (file.exists && !file.delete)
        throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
    }

    deleteRecursively(new File(input))
  }

  def getHashString(input: String): String = {
    import java.security.MessageDigest
    valueHexOf(MessageDigest.getInstance("MD5").digest(input.getBytes).toList)
  }
}