package models

import play.api.data.Form
import play.api.data.Forms._

case class LookFor(name: String)

object LookFor {
    val lookForm: Form[LookFor] = Form(mapping(
        "name" -> text
    )(LookFor.apply)(LookFor.unapply))
}
