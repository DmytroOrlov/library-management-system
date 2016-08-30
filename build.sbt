import sbt.Project.projectToRef

lazy val playSlickV = "2.0.2"
lazy val scribejavaV = "3.1.0"

lazy val server = project
  .enablePlugins(PlayScala)
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJvm)
  .settings(
    inThisBuild(Seq(
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.11.8"
    )),
    testSettings,
    name := "library-management-system",
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    includeFilter in(Assets, LessKeys.less) := "*.less",
    libraryDependencies ++= Seq(
      ws,
      "com.github.scribejava" % "scribejava-apis" % scribejavaV,
      "com.github.scribejava" % "scribejava-httpclient-ahc" % scribejavaV,
      "org.webjars" % "jquery" % "2.2.4",
      "org.webjars" % "bootstrap" % "3.3.6" exclude("org.webjars", "jquery"),
      "com.vmunier" %% "play-scalajs-scripts" % "0.5.0",

      "com.typesafe.play" %% "play-slick" % playSlickV,
      "com.typesafe.play" %% "play-slick-evolutions" % playSlickV,
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.h2database" % "h2" % "1.4.193"
    )
  )

lazy val client = project
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJs)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1"
    )
  )

lazy val clients = Seq(client)

lazy val shared = crossProject.crossType(CrossType.Pure)
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
  )
)

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
