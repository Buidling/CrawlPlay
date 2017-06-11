package controllers

import models.{LookFor, Song}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Flash}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by samsung on 2017/6/7.
  */
object MV extends Controller {

    var op: Option[Array[Song]] = None

    def search = Action { implicit request =>
        Ok(views.html.song.search(LookFor.lookForm))
    }

    def search_detail = Action { implicit request =>
        Ok(views.html.song.result(LookFor.lookForm)(op))
    }

    def doSearch() = Action { implicit request =>
        val lookForm = LookFor.lookForm.bindFromRequest()
        lookForm.fold(
            hasErrors = { form =>
                Redirect(routes.MV.search()).flashing(Flash(form.data) +
                  ("error" -> "could not find this song."))
            },
            success = { form =>
                val str = "http://mvsearch.kugou.com/mv_search?keyword=1&page=1&pagesize=60"
                val pattern = "keyword=1".r
                val url = pattern replaceFirstIn(str, "keyword=" + form.name)
                val doc = Jsoup.connect(url).timeout(10000).get()
                val first = doc.toString.indexOf("{")
                val last = doc.toString.lastIndexOf("}")
//                println(doc.toString.substring(first, last+1))
                val jsonObj = Json.parse(doc.toString.substring(first,last+1))
                val list = (jsonObj \ "data") \ "lists"
                val names = (list \\ "MvName").toArray
                val ids = (list \\ "MvID").toArray
                val pictures = (list \\ "Pic").toArray
                val singers = (list \\ "SingerName").toArray
//                println(ids.length)
//                println(pictures.length)
//                println(singers.length)
                pictures foreach( a => println(a.toString))
                val aBuffer = new ArrayBuffer[Song]()
                for (i <- 0 until names.size) {
                    val name = "[\"]".r replaceAllIn(names(i).toString, "")
                    val pi = "[\"]".r replaceAllIn(ids(i).toString, "")
                    val id = "http://www.kugou.com/mvweb/html/mv_" + pi + ".html"
                    val pp = "[\"]".r replaceAllIn(pictures(i).toString, "")
                    val picture = "http://imge.kugou.com/mvhdpic/240/" + pp.substring(0, 8) + "/" + pp
                    val singer = "[\"]".r replaceAllIn(singers(i).toString, "")
                    println(name)
                    println(id)
                    println(picture)
                    println(singer)
                    val song = Song(id, picture, name, singer)
                    aBuffer append(song)
                }
                op = Some(aBuffer.toArray)
                Redirect(routes.MV.search_detail()).flashing("singer" -> form.name)

//                以下为正则表达式方法（未完成）
//                val p_MvName = "MvName\":\".*?\"".r
//                val p_MvID = "MvID\":\\d*".r
//                val p_Pic = "Pic\":\".*?\"".r
//                val p_SingerName = "SingerName\":\".*?\"".r
//                val mvNames = Option(p_MvName findAllIn doc.toString)
//                val mvID = Option(p_MvID findAllIn doc.toString)
//                val pics = Option(p_Pic findAllIn doc.toString)
//                val singers = Option(p_SingerName findAllIn doc.toString)
//                mvNames.get.foreach{println}
//                mvID.get.foreach{println}
//                pics.get.foreach{println}
//                singers.get.foreach{println}
//                mvNames.get.foreach { str =>
//                    val p = "\".+\"".r findAllIn(str)
//                    p foreach { s =>
//                        "[:\"]".r replaceAllIn(s, "")
//                    }
//                }
//                val songList: List[Song] = for (link <- Links) {
//
//                }
            }
        )
    }
}
