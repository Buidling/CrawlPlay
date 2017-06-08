package controllers

import models.LookFor
import play.api.mvc.{Action, Controller, Flash}

/**
  * Created by samsung on 2017/6/7.
  */
object Search extends Controller {

    def search = Action { implicit request =>
        Ok(views.html.song.search(LookFor.lookForm))
    }

    def search_detail(name: String) = Action { implicit request =>
        Ok(views.html.song.result(name))
    }

    def doSearch() = Action { implicit request =>
        val lookForm = LookFor.lookForm.bindFromRequest()
        lookForm.fold(
            hasErrors = { form =>
                Redirect(routes.Search.search()).flashing(Flash(form.data) +
                  ("error" -> "could not find this song."))
            },
            success = { form =>
                Redirect(routes.Search.search_detail(form.name))
            }
        )
    }
}
