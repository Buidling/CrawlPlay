package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by samsung on 2017/6/8.
  */
case class Login(name: String, password: String)

object Login {
    val loginForm: Form[Login] = Form(mapping(
        "name" -> nonEmptyText,
        "password" -> nonEmptyText
    )(Login.apply)(Login.unapply))
}
