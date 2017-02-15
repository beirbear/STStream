package STStream.Metadata

import java.io.{File, FileNotFoundException}

import STStream.Generals.{Logger => Log}
import STStream.{Definitions => Def}
import STStream.Generals.{Services => Service}

import scala.collection.mutable.ListBuffer

/**
  * Created by beir on 2/6/17.
  */
object MInterface {
  val metaDataFile: String = Def.getMetaFilePath
  val storagePath: String = Def.getStoragePath
  val archivePath: String = Def.getArchivePath
  var dataList: ListBuffer[Record] = new ListBuffer[Record]()

  def resetMetaData = {
    if (!STStream.Generals.Services.isFileFolderExist(Def.getStoragePath))
      Log.errorExit(Def.getMetaFilePath + " doesn't exist!", -1)

    // Get all folder in storage path
    val inputStorage = Service.getFoldersInFolder(storagePath)
    val rawList: ListBuffer[Record] = new ListBuffer[Record]()
    for (line <- inputStorage) {
      val fileName: String = Service.getFileName(line.toString)
      val filePath: String = line.toString
      val isFetch: Boolean = false
      val isArchived: Boolean = false

      rawList += Record(fileName, filePath, "", 0, isArchived, isFetch)
    }

    // Check files in the archived folder
    val inputArchive = Service.getFilesInFolder(archivePath)
    for (line <- inputArchive) {
      println("Processing: " + line.toString)
      val fileName: String = Service.getFileNameWithoutExtension(line.toString)
      val filePath: String = line.toString
      val fileSize: Long = new File(line.toString) .length
      val hashV: String = getHash(fileName)
      val isArchived: Boolean = true
      val isFetched: Boolean  = false

      rawList += Record(fileName, filePath, hashV, fileSize, isArchived, isFetched)
    }

    dataList = rawList
  }

  private def getHash(input: String): String = {

    def isDuplicatedHash(input: String): Boolean = {
      dataList.exists(e => e.hash == input)
    }

    var hashStr = Service.getHashString(input)
    val r = scala.util.Random
    do {
      hashStr = Service.getHashString(input + r.nextInt.toString)
    } while (isDuplicatedHash(hashStr))
    hashStr
  }

  def scanForChanged = {
    // Step 1: Scan for new folder in storage path
    if (!Service.isFileFolderExist(storagePath))
      throw new FileNotFoundException("Storage path doesn't exist!")

    // Get all folder in storage path
    val inputStorage = Service.getFoldersInFolder(storagePath)
    for (line <- inputStorage) {
      // Check for the existing item in meta table
      val tmp = Service.getFileName(line.toString)
      val res = dataList.filter(e => e.fileName == tmp)

      if (res.isEmpty){
        val fileName: String = tmp
        val filePath: String = line.toString
        val isFetch: Boolean = false
        val isArchived: Boolean = false
        dataList += Record(fileName, filePath, "", 0, isArchived, isFetch)
      }
    }
  }

  def zipContent: Unit = {
    for( item <- dataList ) {
      if (!item.isArchived) {
        val srcFolder = item.filePath.toString
        Service.zipFolder(item.filePath.toString, Def.getArchivePath + item.fileName)
        item.isArchived = true
        item.hash = getHash(item.fileName)
        item.filePath = Def.getArchivePath+ item.fileName + ".zip"
        item.fileSize = new File(item.filePath) .length
        Service.deleteFolder(srcFolder)
        Log.write("Source folder is deleted: " + srcFolder)
      }
    }
  }

  def isValidFile(input: String): Boolean = {
    dataList.exists(e => e.hash == input)
  }

  def getContent(input: String): Array[Byte] = {
    val line = dataList.filter(e => e.hash == input)
    if (line.isEmpty) {
      throw new Exception("Invalid hash reference!")
    }

    // Flag as downloaded.
    line.head.isFetched = true
    writeMetaFile

    import java.nio.file.{Files, Paths}
    val byteArray = Files.readAllBytes(Paths.get(line.head.filePath))

    byteArray
  }

  def getDownloadIndex: String = {
    val tmp = dataList.filter(e => e.isArchived && !e.isFetched)
    if (tmp.isEmpty) {
      ""
    }
    else {
      val rString: StringBuffer = new StringBuffer()
      for (line <- tmp.toList)
        rString.append(line.hash + "\n")
      rString.toString
    }
  }


  def readMetaData: Unit = {
    if (!Service.isFileFolderExist(metaDataFile))
      throw new FileNotFoundException("Metadata doesn't exist!")

    def getStringBoolean(input: String): Boolean = if (input == "true" ) true else false

    val localDataList: ListBuffer[Record] = new ListBuffer[Record]()

    // Read meta data file
    import scala.io.Source
    for (line <- Source.fromFile(metaDataFile).getLines) {
      val tmp = line.split('|')
      localDataList += Record(tmp(0), tmp(1), tmp(2), tmp(3).toLong, getStringBoolean(tmp(4)), getStringBoolean(tmp(5)))
    }
    dataList = localDataList
  }

  def writeMetaFile: Unit = {
    if (!Service.isFileFolderExist(storagePath))
      throw new FileNotFoundException("Storage path doesn't exist!")

    // Start write file
    import java.io._
    val file = new File(metaDataFile)
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- dataList)
      bw.write(s"${line.fileName}|${line.filePath}|${line.hash}|${line.fileSize}|${line.isArchived}|${line.isFetched}\n")
    bw.close()
  }

  def printMeta: Unit = dataList.foreach(println(_))

  def getDataListString: String = {
    val st = new StringBuffer()
    for (item <- dataList)
      st.append(item.toString + "\n")

    st.toString
  }

  def removeDownloadedContent(): Unit = {
    dataList.filter(e => e.isArchived && e.isFetched).foreach(e => Service.deleteFolder(e.filePath))
    scanForChanged
  }
}
