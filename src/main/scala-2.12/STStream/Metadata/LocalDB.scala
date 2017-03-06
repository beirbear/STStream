package STStream.Metadata

import STStream.{Definitions => Def}

import java.io.{BufferedWriter, File, FileWriter}
import java.util.Date

import scala.collection.mutable.ListBuffer

/**
  * Created by beir on 1/28/17.
  */
object LocalDB {
  val stockRecord = new ListBuffer[SInfo]()
  val consensusRecord = new ListBuffer[CInfo]()

  def pushStockInfo(input: ListBuffer[SInfo]): Unit = {
    for (item <- input) {
      if (!stockRecord.exists(e => e.fetchTime == item.fetchTime && e.stockName == item.stockName)) {
        // Append data to the list
          stockRecord.append(item)
        }
    }
    stockRecord.sortBy(_.fetchTime)
  }

  def pushConsensusInfo(input: ListBuffer[ListBuffer[CInfo]]): Unit = {
    for (item <- input) {
      for (eachItem <- item) {
        if (!consensusRecord.exists(e => e.groupDate == eachItem.groupDate && e.stockName == eachItem.stockName && e.brokerName == eachItem.brokerName)){
          consensusRecord.append(eachItem)
        }
      }
    }
  }

  def writeFile(): Unit = {
    val file = new File(Def.getStockFile)
    val bw = new BufferedWriter(new FileWriter(file))
    for (item <- stockRecord)
      bw.write(item.toFileString)
    bw.close()

    val file1 = new File(Def.getConsensusFile)
    val bw1 = new BufferedWriter(new FileWriter(file1))
    for (item <- consensusRecord)
      bw1.write(item.toFileString)
    bw1.close()
  }

  def getAllStockData: String = {
    val tmp = new StringBuilder()
    for (item <- stockRecord)
      tmp.append(item.toFileString + "\n")
    tmp.toString()
  }

  def getAllConsensusData: String = {
    val tmp = new StringBuilder()
    for (item <- consensusRecord)
      tmp.append(item.toFileString + "\n")
    tmp.toString()
  }

  def readFile(): Unit = {
    def getSInfoFromStr(input: String): SInfo = {
      val tmp = input.split('|')
      new SInfo(new Date(tmp(0).toLong),
        tmp(2),
        tmp(3).toDouble,
        tmp(4),
        tmp(5),
        tmp(6),
        tmp(7),
        tmp(8),
        tmp(9),
        tmp(10),
        tmp(11),
        tmp(12),
        tmp(13),
        tmp(14),
        tmp(15),
        tmp(16),
        tmp(17),
        tmp(18),
        tmp(19),
        true)
    }

    def getCInfoFromStr(input: String): CInfo = {
      val tmp = input.split('|')
      new CInfo(new Date(tmp(0).toLong),
        tmp(2),
        tmp(3),
        tmp(4).toDouble,
        tmp(5).toDouble,
        tmp(6).toDouble,
        tmp(7).toDouble,
        tmp(8).toDouble,
        tmp(9).toDouble,
        tmp(10).toDouble,
        tmp(11).toDouble,
        tmp(12),
        tmp(13)
      )
    }

    if (STStream.Generals.Services.isFileFolderExist(Def.getStockFile)) {
      stockRecord.clear()
      import scala.io.Source
      for(line <- Source.fromFile(Def.getStockFile).getLines()) {
        if (!line.isEmpty)
          stockRecord.append(getSInfoFromStr(line))
      }
    }
    else {
      throw new Exception("File doesn't exist")
    }

    if (STStream.Generals.Services.isFileFolderExist(Def.getConsensusFile)){
      consensusRecord.clear()
      import scala.io.Source
      for(line <- Source.fromFile(Def.getConsensusFile).getLines()) {
        if (!line.isEmpty)
          consensusRecord.append(getCInfoFromStr(line))
      }
    } else {
      throw new Exception("File doesn't exist!")
    }
  }
}
