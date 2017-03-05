package STStream.Metadata

/**
  * Created by beir on 1/7/17.
  */
case class CInfo(lastUpdate: java.util.Date,
                 stockName: String,
                 brokerName: String,
                 year_f: Double,
                 change_p: Double,
                 next_year_f: Double,
                 next_change_p: Double,
                 year_pe: Double,
                 year_pvb: Double,
                 year_div_p: Double,
                 target_price: Double,
                 rec: String,
                 groupDate: String) {

  def toFileString: String = {
    val tmp = lastUpdate.getTime
    s"${tmp}|${lastUpdate}|${stockName}|${brokerName}|${year_f}|${change_p}|${next_year_f}|${next_change_p}|${year_pe}|${year_pvb}|${year_div_p}|${target_price}|${rec}|${groupDate}\n"
  }
}


