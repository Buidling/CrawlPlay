package controllers

import models.Product
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller, Flash}

/**
  * Created by samsung on 2017/6/2.
  */
object Products extends Controller {
    def list = Action { implicit request =>
        val  products = Product.findAll
        Ok(views.html.products.list(products))
    }

    def show(ean: Long) = Action { implicit request =>

        Product.findByEan(ean).map { product =>
            Ok(views.html.products.details(product))
        }.getOrElse(NotFound)
    }

    private val productForm: Form[Product] = Form(
        mapping(
            "ean" -> longNumber.verifying(
                "validation.ean.duplicate", Product.findByEan(_).isEmpty),
            "name" -> nonEmptyText,
            "description" -> nonEmptyText
        )(Product.apply)(Product.unapply)
    )

    def save = Action { implicit request =>
        val newProductForm = productForm.bindFromRequest()
        newProductForm.fold(
            hasErrors = { form =>
                Redirect(routes.Products.newProduct()).
                  flashing(Flash(form.data) +
                    ("error" -> Messages("validation.errors")))
            },
            success = { newProduct =>
                Product.add(newProduct)
                val message = Messages("products.new.success", newProduct.name)
                Redirect(routes.Products.show(newProduct.ean)).
                  flashing("success" -> message)
            }
        )
    }

    def newProduct = Action { implicit request =>
        val form = if (flash.get("error").isDefined)
            productForm.bind(flash.data)
        else
            productForm
        Ok(views.html.products.editProduct(form))
    }
}
