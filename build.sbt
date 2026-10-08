ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "edu.sunway.prg2104"
ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-deprecation",
  "-feature"
)

// ScalaFX ships the Scala wrapper only — the actual JavaFX native libraries
// must be pulled in separately, per OS, or `sbt run` fails with NoClassDefFoundError.
lazy val javaFXOS = System.getProperty("os.name").toLowerCase match {
  case osName if osName.contains("mac")    => "mac"
  case osName if osName.contains("win")    => "win"
  case _                                   => "linux"
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics")

lazy val root = (project in file("."))
  .settings(
    name := "microfinance-loan-tracker",
    version := "0.1.0",

    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "com.lihaoyi" %% "upickle" % "3.3.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ) ++ javaFXModules.map(module =>
      "org.openjfx" % s"javafx-$module" % "21.0.2" classifier javaFXOS
    ),

    fork := true,

    Compile / mainClass := Some("ui.MainApp")
  )
