package controllers

import models.Song
import play.api.mvc.{Action, Controller}

/**
  * Created by samsung on 2017/6/7.
  */
object Search extends Controller {

    def search = Action { implicit request =>
        Ok(views.html.search(Song.songForm))
    }

    def doSearch = TODO
}
