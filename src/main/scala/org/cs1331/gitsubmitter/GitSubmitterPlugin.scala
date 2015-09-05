package org.cs1331.gitsubmitter

import java.time.LocalDateTime
import sbt._
import Keys._

object GitSubmitterPlugin extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += submitCommand)

  lazy val submitCommand = Command.command("submit") { (state: State) =>
    val repoName = "hw-example"
    val submission = Application.authenticateAndCreate(repoName, true)
    submission.createRepo()
    submission.addCollab("cs1331-fall2015")
    val commitMsg = s"Solution progress at ${LocalDateTime.now}"

    submission.pushFiles(commitMsg, "src/main/java/")
    println("Launching browser to view repo...")
    val repoBase = "http://github.gatech.edu"
    val repoUrl = s"$repoBase/${submission.getUser()}/${submission.getRepo()}"
    java.awt.Desktop.getDesktop().browse(new java.net.URI(repoUrl))
    state
  }
}
