package STStream.Metadata

import java.nio.file.Paths
import scala.collection.mutable.ListBuffer

/**
  * Created by beir on 2/6/17.
  */


class DataParser(val srcFolder: String) {

  def transformData: (ListBuffer[SInfo], ListBuffer[ListBuffer[CInfo]]) = {

    def getSInfoDefaultError = SInfo(new java.util.Date(), "_", 0, "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", false)

    import java.io.File
    import scala.collection.mutable.ListBuffer
    import net.ruippeixotog.scalascraper.browser.JsoupBrowser
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

    // Check is src path is valid
    val d = new File(srcFolder)
    if (!d.isDirectory)
      throw new Exception("Folder doesn't exist!")

    def readStockInfo(fileName: String): SInfo = {
      val browser = JsoupBrowser()
      val doc = browser.parseFile(fileName)

      // Extract html content
      val content = doc >> elementList(".content-stt")

      // round border section
      val round_content = (content >> elementList(".round-border") >> texts("h1")).flatten.flatten
      if (round_content.isEmpty)
        return getSInfoDefaultError

      var stock_name: String = ""
      var price_recent:Double = 0
      var price_changed:String = "-"
      var price_changedR:String = "-"
      var dataTime: java.util.Date = new java.util.Date()

      if (round_content.length == 3) {
        def getFileName(input: String): String = {
          val p = Paths.get(input)
          p.getFileName.toString
        }

        def getFileNameWithoutExtension(input: String): String = getFileName(input).replaceFirst("[.][^.]+$", "")

        stock_name = getFileNameWithoutExtension(fileName)

        // info time
        val time1 = content >> texts("span")
        val t = time1.flatten
        val format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        if(t(t.length - 5) == "-")
          return getSInfoDefaultError

        dataTime = format.parse(t(t.length - 5))

        // Check for valid content
        if (round_content(1) == "-")
          return getSInfoDefaultError

        price_recent = round_content(0).replace(",", "").toDouble
        price_changed = round_content(1).replace(",", "")
        price_changedR = round_content(2)
      }
      else {
        stock_name = round_content(0)

        // info time
        val time1 = content >> texts("span")
        val t = time1.flatten
        val format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        if(t(t.length - 5) == "-")
          return getSInfoDefaultError

        dataTime = format.parse(t(t.length - 5))

        // Check for valid content
        if (round_content(1) == "-")
          return getSInfoDefaultError

        price_recent = round_content(1).replace(",", "").toDouble
        price_changed = round_content(2).replace(",", "")
        price_changedR = round_content(3)
      }

      // table section
      val tables = (content >> elementList("table") >> texts("td")).flatten
      val tlTable = tables(0).toList
      val price_closed_prev = tlTable(1).replace(",","")
      val price_open_day = tlTable(3).replace(",","")
      val price_top_day = tlTable(5).replace(",","")
      val price_bottom_day = tlTable(7).replace(",","")
      val price_avg_day = tlTable(9).replace(",","")

      val trTable = tables(1).toList

      val trade_volume = trTable(1).replace(",","")
      val trade_value = trTable(3).replace(",","")
      val par_value = trTable(5).replace(",","")
      val price_ceiling = trTable(7).replace(",","")
      val price_floor = trTable(9).replace(",","")

      val tbTable = tables(2).toList
      val bid_volume = tbTable(0).replace(",","")
      val bid_price = tbTable(1).replace(",","")
      val offer_price = tbTable(2).replace(",","")
      val offer_volume = tbTable(3).replace(",","")

      SInfo(dataTime, stock_name, price_recent, price_changed,price_changedR,price_closed_prev,price_open_day,price_top_day,price_bottom_day,price_avg_day,
        trade_volume,trade_value,par_value,price_ceiling,price_floor,bid_volume, bid_price, offer_price, offer_volume, true)
    }

    def readConsensusInfo(fileName: String): ListBuffer[CInfo] = {
      val browser = JsoupBrowser()
      val doc = browser.parseFile(fileName)

      val content = doc >> elementList(".content-stt")
      // round border section
      val round_content = (content >> elementList(".round-border") >> texts("h1")).flatten.flatten
      val stock_name = round_content(0)

      val file = new File(fileName)
      val curentPath = new File(file.getParent())
      val groupDate = curentPath.getName

      val ccsData: ListBuffer[CInfo] = new ListBuffer[CInfo]()

      val tabContent = doc >> elementList("table")
      val consenTable = tabContent(0) >> elementList("tr")
      for(row <- consenTable) {
        val s = row >> texts("td")
        val line = s.toList
        if (line.length == 13) {
          // println(line)
          val broker = line(1)
          val year_f = line(2).replace("-","0").replace(",","").toDouble
          val change_p = line(3).replace("-","0").replace(",","").toDouble
          val next_year_f = line(4).replace("-","0").replace(",","").toDouble
          val next_change_p = line(5).replace("-","0").replace(",","").toDouble
          val year_pe = line(6).replace("-","0").replace(",","").toDouble
          val year_pvb = line(7).replace("-","0").replace(",","").toDouble
          val year_div_p = line(8).replace("-","0").replace(",","").toDouble
          val target_price = line(9).replace("-","0").replace(",","").toDouble
          val rec = line(10)
          val date = line(11)

          val format = new java.text.SimpleDateFormat("dd/MM/yyyy")
          val dataTime = format.parse(date)

          ccsData += CInfo(dataTime, stock_name, broker,year_f,change_p,next_year_f,next_change_p,year_pe,year_pvb,year_div_p,target_price,rec, groupDate)
        }
      }
      ccsData
    }

    val inputFolders = d.listFiles.filter(_.isDirectory).toList
    println("Total records: " + inputFolders.length)

    val sList = new ListBuffer[SInfo]()
    val cList = new ListBuffer[ListBuffer[CInfo]]()

    for (inputFolder <- inputFolders) {
      println("folder: " + inputFolder.toString)
      val f = new File(inputFolder.toString)
      val inputFiles = f.listFiles.filter(_.isFile).toList
      for (inputFile <- inputFiles) {
        val sf = new File(inputFile.toString)
        if (sf.length > 0) {
          if (inputFile.toString.endsWith(".html")) {
            if (inputFile.toString.endsWith(".ccs.html")) {
              try {
                val s = readConsensusInfo(inputFile.toString)
                if (s.nonEmpty) {
                  cList.append(s)
                  // println(s)
                }
              }
              catch {
                case e: Exception => println("Processing exception in " + inputFile.toString + "\r\n" + e.toString)
                  System.exit(1)
              }
            }
            else {
              try {
                val s = readStockInfo(inputFile.toString)
                if (s.isValid) {
                  sList.append(s)
                  // println(s)
                }
              } catch {
                case e: Exception => println("Processing exception in " + inputFile.toString + "\r\n" + e.toString)
                  System.exit(1)
              }
            }
          }
        }
        else {
          println("Empty content detected: " + inputFile.toString)
        }
      }
    }
    (sList, cList)
  }
}

