package org.cs1331.gitsubmitter

import java.time.LocalDateTime
import sbt._
import Keys._

object GitSubmitterPlugin extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += submitCommand)

  lazy val submitCommand = Command.command("submit") { (state: State) =>
    val extracted: Extracted = Project.extract(state)
    import extracted._
    val projectName = (Keys.name in currentRef get structure.data).get
    val organization = (Keys.organization in currentRef get structure.data).get
    val authenticatedUser = AuthenticatedUser.create()
    val repoName = s"${projectName}-${authenticatedUser.name}"
    val submission = new StudentSubmission(authenticatedUser, repoName)
    submission.createRepo()

    // Keys.organization should match the GitHub org name for the current semester
    submission.addCollab(organization)
    val commitMsg = s"Solution progress at ${LocalDateTime.now}"
    submission.pushFiles(commitMsg, "src/main/java/")
    println("Launching browser to view repo...")
    val repoBase = "http://github.gatech.edu"
    val repoUrl = s"$repoBase/${submission.getUser()}/${submission.getRepo()}"
    java.awt.Desktop.getDesktop().browse(new java.net.URI(repoUrl))
    state
  }
}
