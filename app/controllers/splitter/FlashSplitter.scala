package controllers.splitter

object FlashSplitter {
  var emptyString = ""

  def split(isFlash: Boolean, data: String, index: Int) = {
    if(isFlash) {
      if(data.contains("#")) {
        data.split("#").toList(index)
      } else {
        if(index == 1) emptyString
        else data
      }
    } else {
      if(data.contains("&")) data.split("&").toList(index)
      else emptyString
    }
  }
}
