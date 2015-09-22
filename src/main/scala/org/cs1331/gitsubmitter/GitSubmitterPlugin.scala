package org.cs1331.gitsubmitter

import java.time.LocalDateTime
import java.io.UncheckedIOException
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import sbt._
import Keys._

object GitSubmitterPlugin extends AutoPlugin {

  lazy val filesToSubmit = settingKey[Seq[String]]("Tuple of files to submit")
  lazy val collabs = settingKey[Seq[String]]("Tuple of users to add as collaborators")
  override lazy val projectSettings = Seq(commands += submitCommand)

  lazy val submitCommand = Command.command("submit") { (state: State) =>

    LogManager.getLogManager().reset();
    Logger.getLogger("").addHandler(new FileHandler("%t/git-submitter%g.log"));

    val extracted: Extracted = Project.extract(state)
    import extracted._
    val projectName = (Keys.name in currentRef get structure.data).get
    val organization = (Keys.organization in currentRef get structure.data).get
    val authenticatedUser = AuthenticatedUser.create()
    val repoName = s"${projectName}-${authenticatedUser.name}"
    val submission = new StudentSubmission(authenticatedUser, repoName)
    val submissionFiles = (filesToSubmit in currentRef get structure.data).get
    val collabUsers = (collabs in currentRef get structure.data).get
    println("Submitting files...")
    submission.createRepo()

    collabUsers.foreach(c => submission.addCollab(c))

    val commitMsg = s"Solution progress at ${LocalDateTime.now}"
    var pushSucceeded = true
    try {
      submission.pushFiles(commitMsg, submissionFiles: _*)
    } catch {
      case ioe: UncheckedIOException => {
        println("Couldn't push files: " + submissionFiles)
        println(ioe.getMessage)
        ioe.printStackTrace()
        pushSucceeded = false
      }
    }
    if (pushSucceeded) {
      println("Launching browser to view repo...")
      val repoBase = "http://github.gatech.edu"
      val repoUrl = s"$repoBase/${submission.getUser()}/${submission.getRepo()}"
      java.awt.Desktop.getDesktop().browse(new java.net.URI(repoUrl))
      state
    } else {
      state.fail
    }
  }
}
