package STStream.Metadata

/**
  * Created by beir on 1/1/17.
  */

case class SInfo(fetchTime: java.util.Date,
                 stockName: String,
                 priceRecent: Double,
                 priceChanged: String,
                 priceChangeR: String,
                 priceClosedPrev: String,
                 priceOpenDay: String,
                 priceTopDay: String,
                 priceBottomDay: String,
                 priceAvgDay: String,
                 tradeVolume: String,
                 tradeValue: String,
                 parValue: String,
                 priceCeiling: String,
                 priceFloor: String,
                 bidVolume: String,
                 bidPrice: String,
                 offerPrice: String,
                 offerVolume: String,
                 isValid: Boolean) {

  def toFileString: String = {
    val tmp = fetchTime.getTime
    s"${tmp}|${fetchTime}|${stockName}|${priceRecent}|${priceChanged}|${priceChangeR}|${priceClosedPrev}|${priceOpenDay}|${priceTopDay}|${priceBottomDay}|${priceAvgDay}|${tradeVolume}|${tradeValue}|${parValue}|${priceCeiling}|${priceFloor}|${bidVolume}|${bidPrice}|${offerPrice}|${offerVolume}\n"
  }
}


