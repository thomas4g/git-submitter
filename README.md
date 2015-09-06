Submission Tool
============

What It Does
------------
authenticates student against the GT github api, creates a repo under their name, and adds us as collaborators

Building
--------
For developers of this tool, use `sbt publish` if you are trying to publish a new version and have been given repo access. Otherwise, use `sbt compile` to build.


Usage
-------
To use the tool, you'll need a project that includes the SBT plugin. You can install the plugin by adding:

        resolvers += "CS 1331 Repository" at "http://repo.tweedbathrobe.com"
        addSbtPlugin("org.cs1331" % "git-submitter-plugin" % "1.0-SNAPSHOT")

to your `project/plugins.sbt` or `project/buildinfo.sbt`

Next, make sure your project settings has  `name` and `organization` keys. Finally, add the `submit` command to your project with:

        Keys.commands += org.cs1331.gitsubmitter.GitSubmitterPlugin.submitCommand
