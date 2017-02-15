package STStream.Metadata

/**
  * Created by beir on 2/7/17.
  */
case class Record(fileName: String,
                  var filePath: String,
                  var hash: String,
                  var fileSize: Long,
                  var isArchived: Boolean,
                  var isFetched: Boolean)
