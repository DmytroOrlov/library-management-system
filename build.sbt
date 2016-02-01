import sbt.Project.projectToRef

lazy val clients = Seq(client)

lazy val commonSettings = Seq(scalaVersion := "2.11.7")

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
    "org.scalatestplus" %% "play" % "1.4.0" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
  )
)

lazy val server = (project in file("server"))
  .settings(commonSettings ++ testSettings: _*)
  .settings(
    routesGenerator := InjectedRoutesGenerator,
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
      "org.webjars" % "jquery" % "2.2.0",
      "org.webjars.bower" % "epoch" % "0.6.0",
      "org.webjars" % "d3js" % "3.5.12",
      "org.monifu" %% "monifu" % "1.0",
      "com.typesafe.play" %% "play-slick" % "1.1.1",
      "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1",
      "org.postgresql" % "postgresql" % "9.4.1207",
      "com.h2database" % "h2" % "1.4.190"
    )
  ).enablePlugins(PlayScala)
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJs)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
//    sourceMapsDirectories += sharedJs.base / "..",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.2",
      "org.monifu" %%% "monifu" % "1.0"
    )
  )

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings: _*)
  .jsConfigure(_ enablePlugins ScalaJSPlay)
//  .jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
