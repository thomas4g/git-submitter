lazy val root = (project in file(".")).
  settings(
    name := "git-submitter-plugin",
    version := "1.0-SNAPSHOT",
    sbtPlugin := true,
    scalaVersion := "2.10.4",
    organization := "org.cs1331",
    publishMavenStyle := true,
    publishTo := Some(Resolver.sftp("CS 1331 Repository",
      "repo.tweedbathrobe.com", "repo.tweedbathrobe.com/")),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.12"
    )
  )
