package org.cs1331.gitsubmitter

import java.time.LocalDateTime
import sbt._
import Keys._

object GitSubmitterPlugin extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += submitCommand)

  lazy val submitCommand = Command.command("submit") { (state: State) =>
    val prismId = readLine("Enter your prism id: ")
    val repoSuffix = s"-$prismId"
    val submission = Application.authenticateAndCreate(repoSuffix)
    submission.createRepo()
    submission.addCollab("cs1331-fall2015")
    val commitMsg = s"Solution progress at ${LocalDateTime.now}"

    // This line fails. I don't know how to use StudentSubmission.pushFile ...
    // it's not like git-add, which you can just hand a directory. Maybe
    // we need to list all the files in src/main/java and 'push' them one by one?
    // That seems inelegant.
    submission.pushFile(new File("src/main/java/"), commitMsg)
    println("Launching browser to view repo...")
    val repoBase = "http://github.gatech.edu"
    val repoUrl = s"$repoBase/${submission.getUser()}/${submission.getRepo()}"
    java.awt.Desktop.getDesktop().browse(new java.net.URI(repoUrl))
    state
  }
}
