package controllers

import models.{LookFor, Song}
import models.Constants._
import org.jsoup.Jsoup
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Flash}
import anorm.SqlParser._
import anorm._
import play.api.Play.current

import scala.collection.mutable.ArrayBuffer

/**
  * Created by samsung on 2017/6/7.
  */
object MV extends Controller {

    var op: Option[Array[Song]] = None
    var op_collect: Option[Array[Song]] = None

    def search = Action { implicit request =>
        Ok(views.html.song.search(LookFor.lookForm))
    }

    def search_detail = Action { implicit request =>
        op match {
            case Some(array) => println("user_name is not null")
            case None => searching(session.get("user_name").get)
        }
        Ok(views.html.song.result(LookFor.lookForm)(op))
    }

    def myCollect = Action { implicit request =>
        Ok(views.html.song.collect(LookFor.lookForm)(op_collect))
    }

    def doSearch() = Action { implicit request =>
        val lookForm = LookFor.lookForm.bindFromRequest()
        lookForm.fold(
            hasErrors = { form =>
                Redirect(routes.MV.search()).flashing(Flash(form.data) +
                  ("error" -> "could not find this song."))
            },
            success = { form =>
//                println("session singer: " + session.get("singer"))
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
                val aBuffer = new ArrayBuffer[Song]()
                for (i <- 0 until names.size) {
                    val name = "[\"]".r replaceAllIn(names(i).toString, "")
                    val pi = "[\"]".r replaceAllIn(ids(i).toString, "")
                    val id = "http://www.kugou.com/mvweb/html/mv_" + pi + ".html"
                    val pp = "[\"]".r replaceAllIn(pictures(i).toString, "")
                    val picture = "http://imge.kugou.com/mvhdpic/240/" + pp.substring(0, 8) + "/" + pp
                    val singer = "[\"]".r replaceAllIn(singers(i).toString, "")
//                    println(name)
//                    println(id)
//                    println(picture)
//                    println(singer)
                    val song = Song(id, picture, name, singer)
                    aBuffer append(song)
                }
                op = Some(aBuffer.toArray)
                Redirect(routes.MV.search_detail()).withSession(session + ("singer" -> form.name))

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

    /**
      * 保存mv内容到mysql
      * @return
      */
    def save_mv = Action { implicit request =>
        val aBuffer = new ArrayBuffer[String]()
        println("save mv")
        for( i <- 0 until mvCount) {
            val ids = request.cookies.get("mvId" + i)
            ids match {
                case Some(cookie) => {
                    println(cookie.value)
                    aBuffer append(cookie.value)
                }
                case None => println("cookie" + i + "is empty")
            }
        }
        searching(session.get("singer").get)
        val array = aBuffer.toArray
        val arraySong = op.get
        val user_name = session.get("user_name").get
        for (i <- array.indices) {
            val j = array(i).toInt
            DB.withConnection { implicit c =>
                val result = {
                    SQL("select user_name,href from mv where user_name={user_name} and href={href}").on(
                        "user_name" -> user_name, "href" -> arraySong(j).href
                    ).as(str("user_name") ~ str("href") *)
                }
                println("result isEmpty?: " + result.isEmpty)
                if (result.isEmpty) {
                    SQL("insert into mv(href, img, mv_name, mv_singer, user_name) values (" +
                      "{href}, {img}, {mv_name}, {mv_singer}, {user_name})").on("href" -> arraySong(j).href,
                    "img" -> arraySong(j).img, "mv_name" -> arraySong(j).name, "mv_singer" -> arraySong(j).singer,
                    "user_name" -> user_name).executeUpdate()
                } else {
                    println("This song has already exit in your collection: " + arraySong(j).name)
                }
            }
        }
        Redirect(routes.MV.search_detail())
    }

    /**
      * 根据输入的字符串查找mv
      * @param name
      */
    def searching(name: String): Unit = {
        val str = "http://mvsearch.kugou.com/mv_search?keyword=1&page=1&pagesize=60"
        val pattern = "keyword=1".r
        val url = pattern replaceFirstIn(str, "keyword=" + name)
        val doc = Jsoup.connect(url).timeout(10000).get()
        val first = doc.toString.indexOf("{")
        val last = doc.toString.lastIndexOf("}")
        val jsonObj = Json.parse(doc.toString.substring(first,last+1))
        val list = (jsonObj \ "data") \ "lists"
        val names = (list \\ "MvName").toArray
        val ids = (list \\ "MvID").toArray
        val pictures = (list \\ "Pic").toArray
        val singers = (list \\ "SingerName").toArray
        val aBuffer = new ArrayBuffer[Song]()
        for (i <- 0 until names.size) {
            val name = "[\"]".r replaceAllIn(names(i).toString, "")
            val pi = "[\"]".r replaceAllIn(ids(i).toString, "")
            val id = "http://www.kugou.com/mvweb/html/mv_" + pi + ".html"
            val pp = "[\"]".r replaceAllIn(pictures(i).toString, "")
            val picture = "http://imge.kugou.com/mvhdpic/240/" + pp.substring(0, 8) + "/" + pp
            val singer = "[\"]".r replaceAllIn(singers(i).toString, "")
            val song = Song(id, picture, name, singer)
            aBuffer append(song)
        }
        op = Some(aBuffer.toArray)
    }

    /**
      *转跳到收藏页
      */
    def do_collect = Action { implicit request =>
        val aBuffer = new ArrayBuffer[Song]()
        DB.withConnection{ implicit c =>
            val result = SQL(
                """
                  |select * from mv where user_name = {user_name};
                """.stripMargin
            ).on("user_name" -> session.get("user_name").get)
            result() foreach {
                case Row(href: String, img: String, mv_name: String, mv_singer: String, _) =>
                    println("href: " + href)
                    println("img: " + img)
                    println("mv_name: " + mv_name)
                    println("mv_singer: " + mv_singer)
                    val song = Song(href, img, mv_name, mv_singer)
                    aBuffer append(song)
            }
            op_collect = Some(aBuffer.toArray)
        }
        Redirect(routes.MV.myCollect())
    }

    /**
      * 删除mv
      */
    def delete_mv = Action { implicit request =>
        val aBuffer = new ArrayBuffer[String]()
        println("delete mv")
        for( i <- 0 until mvCount) {
            val ids = request.cookies.get("mvId" + i)
            ids match {
                case Some(cookie) => {
                    println(cookie.value)
                    aBuffer append(cookie.value)
                }
                case None => println("cookie" + i + "is empty")
            }
        }
        val array = aBuffer.toList
        op_collect match {
            case Some(arraySong) =>
                val user_name = session.get("user_name").get
                DB.withConnection { implicit c =>
                    array foreach { i =>
                        if (i.toInt < arraySong.length) {
                            SQL("delete from mv where user_name={user_name} and img={img}").on(
                                "user_name" -> user_name, "img" -> arraySong(i.toInt).img).executeUpdate()
                            println("delete mv " + i + ":  " + arraySong(i.toInt).name)
                        }
                    }
                }
            case None => println("op_collect is None")
        }
        Redirect(routes.MV.do_collect())
    }
}
