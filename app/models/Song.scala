package models

import play.api.data.Form
import play.api.data.Forms._

case class Song(href: String, img: String, name: String, singer: String)

object Song {
    val songForm: Form[Song] = Form(mapping(
        "href" -> text,
        "img" -> text,
        "name" -> text,
        "singer" -> text
    )(Song.apply)(Song.unapply))
}
