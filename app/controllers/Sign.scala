package controllers

import models.{Login, User}
import play.api.mvc.{Action, Controller, Flash}
import anorm.SqlParser._
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
        var ch = 0
        userForm.fold(
            hasErrors = { form =>
                Redirect(routes.Sign.signUp()).
                  flashing(Flash(form.data) + ("error" -> "please correct the errors in the form."))
            },
            success = { newUser =>
                try {
                    println("connecting database...")
                    DB.withConnection { implicit c =>
                        val change: Int = SQL("insert into user(user_name, user_phone, user_password) values (" +
                          "{name}, {phone}, {password})").on("name" -> newUser._1,
                            "phone" -> newUser._2, "password" -> newUser._3).executeUpdate()
                        ch = change
                    }
                } catch {
                    case ex: Exception =>
                        println("connected failed: " + ex)
                }
                println("change: " + ch)
                if (ch != 0) {
                    Redirect(routes.Sign.signIn()).flashing("success" -> "Register successfully!")
                } else {
                    Redirect(routes.Sign.signUp()).
                      flashing("success" -> "This name already exit, please try again.")
                }
            }
        )
    }

    def signIn = Action { implicit request =>
        Ok(views.html.sign.signin(Login.loginForm))
    }

    def postSignIn = Action { implicit request =>
        val loginForm = Login.loginForm.bindFromRequest()
        loginForm.fold(
            hasErrors = { form =>
                Redirect(routes.Sign.signIn()).flashing(Flash(form.data) +
                  ("error" -> "Sign in failed, please try again."))
            },
            success = { form =>
                DB.withConnection { implicit c =>
                    val result = {
                        SQL("select * from user where user_name={name} and user_password={password}").on(
                            "name" -> form.name, "password" -> form.password
                        ).as(str("user_name") ~ str("user_password") *)
                    }
                    println("name: " + form.name)
                    println("password: " + form.password)
                    println("select: " + result)
                    if (result.nonEmpty) {
                        Redirect(routes.MV.search()).withSession(session + ("user_name" -> form.name))
                    } else {
                        Redirect(routes.Sign.signIn()).flashing(
                            "success" -> "name or password wrong.")
                    }
                }
            }
        )
    }

}
