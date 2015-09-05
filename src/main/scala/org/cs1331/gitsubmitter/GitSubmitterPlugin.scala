package org.cs1331.gitsubmitter

import java.time.LocalDateTime
import sbt._
import Keys._

object GitSubmitterPlugin extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += submitCommand)

  lazy val submitCommand = Command.command("submit") { (state: State) =>
    val authenticatedUser = AuthenticatedUser.create()
    // This doesn't compile. How the bloody fucking hell do you get the
    // value associated with a SettingKey?
    // Why are the simplest things in SBT so fucking hard?

    // Keys.name is the name of the SBT project for the assignment
    val repoName = s"${Keys.name.value}-${authenticatedUser.name}"
    val submission = new StudentSubmission(authenticatedUser, repoName)
    submission.createRepo()
    // Keys.organization should match the GitHub org name for the current semester
    submission.addCollab(Keys.organization.value)
    val commitMsg = s"Solution progress at ${LocalDateTime.now}"
    submission.pushFiles(commitMsg, "src/main/java/")
    println("Launching browser to view repo...")
    val repoBase = "http://github.gatech.edu"
    val repoUrl = s"$repoBase/${submission.getUser()}/${submission.getRepo()}"
    java.awt.Desktop.getDesktop().browse(new java.net.URI(repoUrl))
    state
  }
}
