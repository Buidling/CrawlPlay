package models

import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by samsung on 2017/5/25.
  */
case class User(name: String, phone: String, password: String)

object User {
    val form = Form(tuple(
        "name" -> nonEmptyText,
        "phone" -> nonEmptyText,
        "password" -> nonEmptyText
    ))
}
