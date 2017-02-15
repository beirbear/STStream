package STStream.Analysis

import scala.collection.mutable.ListBuffer

/**
  * Created by beir on 2/7/17.
  */
object APeriodReview {
  def reviewPeriods = List(10,15,20,20 * 2,20 * 3, 20 * 4,20 * 5, 20 * 6,20 * 9,20 * 12)

  def perriodReview = {
    import java.text.DecimalFormat
    val formatter = new DecimalFormat("##.#####")

    // Step 1: group stock symbol
    val sList = STStream.Metadata.LocalDB.stockRecord.groupBy(_.stockName)
    val cList = STStream.Metadata.LocalDB.consensusRecord.groupBy(_.stockName)

    for((key, value) <- sList) {
      val stockName = key

      // Sort by date
      val tmpList = value.sortBy(_.fetchTime).reverse

      // Convert changed ratio into double
      val changedRList = new ListBuffer[Double]
      val priceList = new ListBuffer[Double]

      for (line <- tmpList) {
        if (line.priceChangeR != "N/A") {
          changedRList.append(line.priceChangeR.replace("%", "").toDouble)
          priceList.append(line.priceRecent)
        }
      }

      for (reviewPeriod <- reviewPeriods) {
        if (changedRList.length >= reviewPeriod) {
          val avgCR = changedRList.slice(0, reviewPeriod).sum / reviewPeriod
          val avgP = priceList.slice(0, reviewPeriod).sum / reviewPeriod
          println("[" + stockName + ": " + reviewPeriod + " days] :\t" + formatter.format(avgCR) + "%\t" + formatter.format(avgP))
        }
      }
      println()
    }

    for((key, value) <- cList) {
      val stockName = key

      val tmpList = value.sortBy(_.lastUpdate).reverse

      for(line <- tmpList)
        println(line.lastUpdate + "\t" + line.stockName + "\t" + line.brokerName + "\t" + line.target_price + "\t" + line.rec)
      println()
    }
  }
}
