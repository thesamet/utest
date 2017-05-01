import org.scalajs.core.tools.sem.CheckedBehavior

def macroDependencies(version: String) =
  ("org.scala-lang" % "scala-reflect" % version) +:
  (if (version startsWith "2.10.")
     Seq(compilerPlugin("org.scalamacros" % s"paradise" % "2.1.0" cross CrossVersion.full),
         "org.scalamacros" %% s"quasiquotes" % "2.1.0")
   else
     Seq())

lazy val utest = crossProject
  .settings(
    name                  := "utest",
    organization          := "com.lihaoyi",
    version               := "0.4.6-SNAPSHOT",
    scalaVersion          := "2.12.2",
    crossScalaVersions    := Seq("2.10.6", "2.11.11", "2.12.2"),
    scalacOptions         := Seq("-Ywarn-dead-code"),
    scalacOptions in Test -= "-Ywarn-dead-code",
    libraryDependencies  ++= macroDependencies(scalaVersion.value),
    updateOptions         := updateOptions.value.withCachedResolution(true),
    incOptions            := incOptions.value.withNameHashing(true).withLogRecompileOnMacro(false),
    triggeredMessage      := Watched.clearWhenTriggered,
    scalacOptions        ++= Seq(scalaVersion.value match {
      case x if x.startsWith("2.12.") => "-target:jvm-1.8"
      case _                          => "-target:jvm-1.6"
    }),

    unmanagedSourceDirectories in Compile += {
      val v = if (scalaVersion.value startsWith "2.10.") "scala-2.10" else "scala-2.11"
      baseDirectory.value/".."/"shared"/"src"/"main"/v
    },
//    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.4" % "provided",
//    autoCompilerPlugins := true,
//    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.4"),
    testFrameworks += new TestFramework("test.utest.CustomFramework"),

    // Sonatype2
    publishArtifact in Test := false,
    publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),

    pomExtra :=
      <url>https://github.com/lihaoyi/utest</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/lihaoyi/utest.git</url>
        <connection>scm:git://github.com/lihaoyi/utest.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lihaoyi</id>
          <name>Li Haoyi</name>
          <url>https://github.com/lihaoyi</url>
        </developer>
      </developers>
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion
    // scalaJSSemantics in Test ~= (_.withAsInstanceOfs(CheckedBehavior.Compliant))
  )
  .jvmSettings(
//    fork in Test := true,
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0",
      "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
    ),
    resolvers += Resolver.sonatypeRepo("snapshots")
  )

lazy val utestJS = utest.js
lazy val utestJVM = utest.jvm

