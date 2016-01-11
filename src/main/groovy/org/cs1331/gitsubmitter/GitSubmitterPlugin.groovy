package org.cs1331.gitsubmitter

import org.gradle.api.Project
import org.gradle.api.Plugin

class GitSubmitterPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("submission", SubmitterPluginExtension)
        project.task('submit') << {
            def username = System.console().readLine('\nUsername (e.g, gburdell3): ');
            def password = System.console().readPassword('Password: ');
            List<String> args = []
            args << p(Application.ADD_COLLABS_OPT)
            args.addAll(project.submission.collabs)

            args << p(Application.SHOW_SUBMISSION_OPT)

            args << p(Application.REPO_NAME_OPT)
            args << project.submission.repo_name

            args << p(Application.USERNAME_OPT)
            args << username

            args << p(Application.PASSWORD_OPT)
            args << password

            Application.main(args)
        }
    }

    String p(String s) {
        return "-" + s
    }
}
