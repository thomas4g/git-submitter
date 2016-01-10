package org.cs1331.gitsubmitter;

import java.awt.Desktop;
import java.net.UnknownHostException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Application {

    private static final String DEFAULT_CONFIG = "json";

    private static boolean checkCompile(String[] files) throws Exception {
        String combined = Arrays.stream(files).collect(Collectors.joining(" "));
        List<String> args = Arrays.stream(combined.split(" ")).filter(f -> f
            .endsWith(".java")).collect(Collectors.toList());
        if (args.size() == 0) {
            return true;
        }
        args.add(0, "javac");
        ProcessBuilder pcb = new ProcessBuilder(args);
        Process p = pcb.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    private static final String SUBMISSION_OPT  = "s";
    private static final String COMPILE_OPT     = "j";
    private static final String REPO_OPT        = "r";
    private static final String COMMIT_OPT      = "m";
    private static final String COLLABS_OPT     = "c";
    private static final String FILES_OPT       = "f";


    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption(SUBMISSION_OPT, "confirm-submission", false,
            "Open submission in web browser to confirm successful submission");
        options.addOption(COMPILE_OPT, "compile", false,
            "Check the submission compiles before submitting and reject if it does not.");
        options.addOption(Option.builder(REPO_OPT)
            .required()
            .longOpt("repo-name")
            .hasArg()
            .argName("repository_name")
            .desc("Required: The name of the repository")
            .build());

        options.addOption(Option.builder(COMMIT_OPT)
            .longOpt("commit-message")
            .hasArg()
            .argName("message")
            .desc("An optional commit message. If none is specified, the current time will be used")
            .build());

        options.addOption(Option.builder(COLLABS_OPT)
            .longOpt("collabs")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .argName("user1 user2 user3...")
            .desc("An optional list of users to add as collaborators (who will have fork "
                + "privileges")
            .build());

        options.addOption(Option.builder(FILES_OPT)
            .longOpt("files")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .argName("file1 file2 file3...")
            .desc("An optional list of files to submit. If no files are specified, src/main/java "
                + "will be recursively searched for Java files")
            .build());


        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            formatter.printHelp("cmd_to_run", options);
            System.exit(1);
        }


        try {

            String[] files = {"src/main/java"};
            if (line.hasOption(FILES_OPT)) {
                files = line.getOptionValues(FILES_OPT);
            }
            files = Arrays.stream(files).map(s -> s.replaceAll("'", "")).toArray(String[]::new);

            if (line.hasOption(COMPILE_OPT)) {
                System.out.print("Compiling files.....");
                if (!checkCompile(files)) {
                    System.out.println("\nYour submission does not compile. "
                        + "Please fix your compile errors before proceeding.");
                    System.exit(1);
                }
                System.out.println("...done");
            }

            String repoName = line.getOptionValue(REPO_OPT);

            AuthenticatedUser user = AuthenticatedUser.create();
            StudentSubmission sub = new StudentSubmission(user, repoName);
            sub.createRepo();

            if (line.hasOption(COLLABS_OPT)) {
                for (String collab : line.getOptionValues(COLLABS_OPT)) {
                    System.out.println("Adding: " + collab);
                    sub.addCollab(collab);
                }
            }

            String commitMsg = "Solution progress at " + LocalDateTime.now();
            if (line.hasOption(COMMIT_OPT)) {
                commitMsg = line.getOptionValue(COMMIT_OPT);
            }

            boolean success = false;
            try {
                success = sub.pushFiles(commitMsg, files);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (success) {
                System.out.println("Code submitted successfully!");
                if (line.hasOption(SUBMISSION_OPT)) {
                    System.out.println("Launching browser to view repo...");
                    Desktop.getDesktop().browse(new URI(
                        String.format("http://github.gatech.edu/%s/%s",
                            sub.getUser(),
                        sub.getRepo())));
                }
            } else {
                System.out.println("Submission unsuccessful. :(");
            }
        } catch (UnknownHostException ex) {
            System.out.println("Internet connection failed. Please repair your"
                + " connection and try again.");
        } catch (Exception e) {
            System.out.println("Something went wrong. Please check your"
                + " internet connection and that the source files are present."
                + " Call a TA for help if you need further assistance.");

            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
