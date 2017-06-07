name := "StudyPlay"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.38"

play.Project.playScalaSettings
