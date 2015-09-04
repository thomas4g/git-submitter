lazy val root = (project in file(".")).
  settings(
    name := "git-submitter",
    version := "1.0-SNAPSHOT",
    autoScalaLibrary := false,
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.4", "2.12.0"),
    organization := "org.cs1331",
    publishMavenStyle := true,
    publishTo := Some(Resolver.sftp("CS 1331 Repository",
      "repo.tweedbathrobe.com", "repo.tweedbathrobe.com/")),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.12"
    )
  )
