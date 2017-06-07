package models

import play.api.data.Form
import play.api.data.Forms._

case class Song(name: String, singer: String, special: String, length: String)

object Song {
    val songForm: Form[Song] = Form(mapping(
        "name" -> text,
        "singer" -> text,
        "special" -> text,
        "length" -> text
    )(Song.apply)(Song.unapply))
}
