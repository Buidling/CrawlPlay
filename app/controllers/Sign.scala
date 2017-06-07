package controllers

import models.User
import play.api.mvc.{Action, Controller, Flash}
import anorm._
import play.api.db.DB
import play.api.Play.current

/**
  * Created by samsung on 2017/5/24.
  */
object Sign extends Controller {

    def signUp = Action { implicit request =>
        Ok(views.html.sign.signup(User.form))
    }

    def postSignUp = Action { implicit request =>
        val userForm = User.form.bindFromRequest()
        userForm.fold(
            hasErrors = { form =>
                Redirect(routes.Sign.signIn()).
                  flashing(Flash(form.data) + ("error" -> "please correct the errors in the form."))
            },
            success = { newUser =>
                try {
                    println("数据库连接中...")
                    DB.withConnection { implicit c =>
                        val result: Int = SQL("insert into user(" +
                          "name, phone, password) values ({name}, {phone}, {password})").on(
                            "name" -> userForm.value.get._1, "phone" -> userForm.value.get._2,
                        "password" -> userForm.value.get._3).executeUpdate()
                    }
                } catch {
                    case ex: Exception => println("连接失败")
                }
                Redirect(routes.Sign.signIn()).flashing("success" -> "Register successfully!")
            }
        )
    }

    def signIn = TODO

    def postSignIn = TODO

}
