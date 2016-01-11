package org.cs1331.gitsubmitter

import org.gradle.api.Project
import org.gradle.api.Plugin

class GitSubmitterPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("submission", SubmitterPluginExtension)
        project.task('submit') << {
            def username = System.console().readLine('\nUsername (e.g, gburdell3): ');
            def password = System.console().readPassword('Password: ');
            println project.submission.repo_name
        }
    }
}
