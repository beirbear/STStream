package STStream

import STStream.Metadata.{DataParser, LocalDB, MInterface}
import STStream.Generals.{Logger => Log}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives.{complete, get, parameters, path, put}
import scala.io.StdIn


/**
  * Created by beir on 2/6/17.
  */
object Main extends App {

  def firstExecution = {
    Log.write("First Execution Processing....")
    Log.write("Scan all files")
    MInterface.resetMetaData
    LocalDB.readFile()

    // Phase 2: Scan for new folders
    Log.write("Scan for changes ....")
    MInterface.scanForChanged

    // Phase 3: Parse new data
    Log.write("Parsing data")
    val dataSource = new DataParser(Definitions.getStoragePath)
    val (sList, cList) = dataSource.transformData
    Log.write("Pushing stock data")
    LocalDB.pushStockInfo(sList)
    Log.write("Pushing consensus data")
    LocalDB.pushConsensusInfo(cList)

    // Phase 4: Zip content
    Log.write("Zip content")
    MInterface.zipContent

    // Phase 5: Write meta file
    Log.write("Save meta data")
    MInterface.writeMetaFile
    LocalDB.writeFile()
  }

  def automatic = {
    MInterface.readMetaData
    LocalDB.readFile()

    // Phase 2: Scan for new folders
    MInterface.scanForChanged

    // Phase 3: Parse new data
    val dataSource = new DataParser(Definitions.getStoragePath)
    val (sList, cList) = dataSource.transformData
    LocalDB.pushStockInfo(sList)
    LocalDB.pushConsensusInfo(cList)

    // Phase 4: Zip content
    MInterface.zipContent

    // Phase 5: Write meta file
    MInterface.writeMetaFile
    LocalDB.writeFile()
  }

  firstExecution
  // STStream.Analysis.APeriodReview.perriodReview

  ///// Start REST API
  implicit val system = ActorSystem("DSMS_Query")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    path("setting") {
      put {
        parameters("command","key") { (command, key) =>
          var responseString = ""
          if (STStream.Generals.SecureKey.isSettingKeyValid(key)) {
            command match {
              case "resetMeta" => Log.write("Setting: Reset meta data requested.")
                MInterface.resetMetaData
                responseString = "Reset Meta Successful!"
              case "readMeta" => Log.write("Setting: Read meta data requested.")
                MInterface.readMetaData
                responseString = "Read Meta Successful!"
              case "updateIndex" => Log.write("Setting: Update meta index requested.")
                MInterface.scanForChanged
                responseString = "Update Meta Successful!"
              case "viewMeta" => Log.write("Setting: View meta requested.")
                responseString = MInterface.getDataListString
              case "removeContent" => Log.write("Setting: Remove downloaded content.")
                MInterface.removeDownloadedContent()
                responseString = "Contents were removed!"
              case "auto" => Log.write("Setting: Auto processing.")
                // Phase 1: Read meta data from file
                automatic
                responseString = "Auto process complete."
              case "extractStock" => Log.write("Setting: extract stock data.")
                responseString = LocalDB.getAllStockData
              case "extractConsensus" => Log.write("Setting: extract consensus data.")
                responseString = LocalDB.getAllConsensusData
              case smtg => Log.warn("Setting: Invalid command requested. > " + smtg)
                responseString = "Invalid command!"
            }
          }
          else {
            // Invalid Setting Key
            responseString = "Invalid Key!!!"
            STStream.Generals.SecureKey.destroySettingKey()
            Log.warn("Invalid setting key submitted!")
          }
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, responseString))
        }
      }
    } ~
    path("query") {
      get {
        parameters("command","key") { (command, key) =>
          println(key)
          var responseString = ""
          if (STStream.Generals.SecureKey.isSecureKeyValid(key)){
            command match {
              case "getIndexes" => Log.write("Query: Download index requested.")
                responseString = MInterface.getDownloadIndex
              case smtg => Log.warn("Query: Invalid command requested. > " + smtg)
                responseString = "Invalid command!"
            }
          }
          else {
            responseString = "Invalid Key!!!"
            STStream.Generals.SecureKey.destroySecureKey()
            Log.warn("Invalid secure key submitted!")
          }
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, responseString))
        }
      }
    } ~
    path("fetch") {
      get {
        parameters("file", "key") { (file, key) =>
          if (STStream.Generals.SecureKey.isSecureKeyValid(key)) {
            if (MInterface.isValidFile(file)) {
              Log.write("Download file > " + file)
              complete(MInterface.getContent(file))
            }
            else {
              Log.warn("Invalid file code and secure key destroyed!")
              STStream.Generals.SecureKey.destroySecureKey()
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Invalid file code!!!!"))
            }
          }
          else {
            STStream.Generals.SecureKey.destroySecureKey()
            Log.warn("Invalid secure key submitted! (DOWNLOAD CMD)")
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Invalid Key!!!!"))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, Definitions.getIpAddr, 8080)

  println(s"Server online at http://${Definitions.getIpAddr}:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done



}

