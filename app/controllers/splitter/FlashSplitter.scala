package controllers.splitter

object FlashSplitter {

  def flashMessageSplitted(flashMessage: String, index: Int) = {
    if(flashMessage.contains("#")) {
      flashMessage.split("#").toList(index)
    } else {
      if(index == 1) {
        ""
      } else {
        flashMessage
      }
    }
  }

  def userDataSplitter(userData: String, index: Int) = {
    if(userData.contains("&")) {
      userData.split("&").toList(index)
    } else {
      ""
    }
  }

}
