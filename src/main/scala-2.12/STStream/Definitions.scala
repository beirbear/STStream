package STStream

/**
  * Created by beir on 2/6/17.
  */
object Definitions {

  def getDataSpace = sys.env("ST_DATA_SPACE")
  def getExtIpAddr = sys.env("ST_IP_ADDR")

  def getMetaFilePath = getDataSpace + "/meta/meta.dat"
  def getStoragePath = getDataSpace + "/storage/"
  def getArchivePath = getDataSpace + "/archive/"
  def getStockFile = getDataSpace + "/meta/stock.dat"
  def getConsensusFile = getDataSpace + "/meta/consensus.dat"
  def getIpAddr = getExtIpAddr
  def getSecureKeyPath = getDataSpace + "/meta/secureKey.key"
  def getSettingKeyPath = getDataSpace + "/meta/settingKey.key"
}
