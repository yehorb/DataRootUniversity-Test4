val globalSettings = Seq[SettingsDefinition](
  version := "0.1",
  scalaVersion := "2.12.4"
)

val Model = project.in(file("Model")).
  settings(globalSettings: _*)

val Repositories = project.in(file("Repositories")).
  settings(globalSettings: _*).
  dependsOn(Model).
  settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
      "org.postgresql" % "postgresql" % "42.1.4"
    )
  )

val Core = project.in(file("Core")).
  settings(globalSettings: _*).
  dependsOn(Model, Repositories)

val CommandLineUI = project.in(file("CommandLineUI")).
  settings(globalSettings: _*).
  dependsOn(Core)

val Application = project.in(file("Application")).
  settings(globalSettings: _*).
  dependsOn(CommandLineUI)

val root = Project("DataRootUniversity-Test4", file(".")).
  aggregate(Application)
