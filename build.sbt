import sbt.Project.projectToRef

lazy val jsProjects = Seq(js)

val monifuVer = "1.2"

val playSlickVersion = "2.0.2"

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala)
  .aggregate(jsProjects.map(projectToRef): _*)
  .dependsOn(sharedJvm)
  .settings(commonSettings ++ testSettings: _*)
  .settings(
    name := "library-management-system",
    scalaJSProjects := jsProjects,
    pipelineStages := Seq(scalaJSProd, gzip),
    libraryDependencies ++= Seq(
      "com.github.scribejava" % "scribejava-apis" % "2.5.3",
      "org.webjars" % "jquery" % "2.2.4",
      "org.webjars" % "bootstrap" % "3.3.6" exclude("org.webjars", "jquery"),
      // todo update to 0.5.0
      "com.vmunier" %% "play-scalajs-scripts" % "0.4.0",
      "org.monifu" %% "monifu" % monifuVer,

      "com.typesafe.play" %% "play-slick" % playSlickVersion,
      "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.h2database" % "h2" % "1.4.191"
    )
  )

lazy val js = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJs)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0",
      "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
      "org.monifu" %%% "monifu" % monifuVer
    )
  )

val scalaVer = "2.11.8"

lazy val commonSettings = Seq(scalaVersion := scalaVer)

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
  )
)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings: _*)
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

scalaJSUseRhino in Global := false // please install nodejs
