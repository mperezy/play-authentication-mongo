package controllers.formerrorconvert

import forms.SignInForm.LoginAccount
import models.Account
import play.api.data.Form

object ConvertToFlashing {

  def convertionFormAccount(form: Form[Account]) = {
    var result = ""
    for(error <- form.globalError) {
      result += error.message
    }
    result
  }

  def convertionFormLoginAccount(form: Form[LoginAccount]) = {
    var result = ""
    for(error <- form.globalError) {
      result += error.message
    }
    result
  }
}
