
val commonSettings = Vector(
  name := "sthocon",
  organization := "org.akka-js",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.4",
  crossScalaVersions  :=
    Vector("2.11.11", "2.12.4")
)

lazy val typesafeShadedConfig = project.in(file("tconfig"))
  .settings(commonSettings)
  .settings(
    name := "shaded-typesafe-config"
  )
  .enablePlugins(AssemblyPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1"
    ),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("com.typesafe.**" -> "ct.@1").inAll
    ),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val sthocon = crossProject.in(file(".")).
  settings(commonSettings).
  settings(
    scalacOptions ++=
      Seq(
        "-feature",
        "-unchecked",
        "-language:implicitConversions"
      )
  ).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    ),
    unmanagedJars in Compile += {
      (assembly in typesafeShadedConfig).value
    }
  ).
  jvmSettings(
  	libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    parallelExecution in Test := true
  )

lazy val sthoconJVM = sthocon.jvm
lazy val sthoconJS = sthocon.js

lazy val root = project.in(file("."))
    .settings(commonSettings)
    .aggregate(sthoconJS, sthoconJVM)
