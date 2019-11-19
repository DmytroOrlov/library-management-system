val V = new {
  val slick = "5.0.0-RC1"
}

lazy val `library-management-system` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % V.slick,
      "com.typesafe.play" %% "play-slick-evolutions" % V.slick,
      "org.postgresql" % "postgresql" % "42.2.8",
      "com.h2database" % "h2" % "1.4.200",

      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0-RC1" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
      "org.mockito" % "mockito-core" % "3.1.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.2" % Test
    )
  )
