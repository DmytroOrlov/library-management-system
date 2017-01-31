lazy val playSlickV = "2.0.2"

lazy val `library-management-system` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-slick" % playSlickV,
      "com.typesafe.play" %% "play-slick-evolutions" % playSlickV,
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.h2database" % "h2" % "1.4.193",

      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % Test,
      "org.mockito" % "mockito-core" % "2.7.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.12.6" % Test
    )
  )
