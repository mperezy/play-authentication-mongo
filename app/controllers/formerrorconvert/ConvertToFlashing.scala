package controllers.formerrorconvert

import forms.SignInForm.LoginAccount
import models.Account
import play.api.data.Form

object ConvertToFlashing {

  def convert[A](form: Form[A]) = {
    var result = ""
    for(error <- form.globalError) {
      result += error.message
    }
    result
  }
}
