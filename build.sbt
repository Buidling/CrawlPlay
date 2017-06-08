name := "StudyPlay"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.38"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.3"

play.Project.playScalaSettings
