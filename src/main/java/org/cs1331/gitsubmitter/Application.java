package org.cs1331.gitsubmitter;

import java.awt.Desktop;
import java.io.File;
import java.net.UnknownHostException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class Application {

    private static final String DEFAULT_CONFIG = "config.json";

    private static boolean checkCompile(String[] files) throws Exception {
        String combined = Arrays.stream(files).collect(Collectors.joining(" "));
        List<String> args = Arrays.stream(combined.split(" ")).filter(f -> f
            .substring(f.lastIndexOf('.')).equals(".java"))
            .collect(Collectors.toList());
        if (args.size() == 0) {
            return true;
        }
        args.add(0, "javac");
        ProcessBuilder pcb = new ProcessBuilder(args);
        Process p = pcb.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    public static StudentSubmission authenticateAndCreate(String repoName,
        boolean appendUserName) throws Exception {
        String user = "", password = "", base64 = "";
        String lines = "------------------------------------------";
        System.out.println();
        System.out.println(lines);
        System.out.println("    Logging in to Georgia Tech servers    ");
        System.out.println(lines);

        boolean success = false;
        boolean twoFactor = false;
        String code = null;
        do {
            if (base64.length() > 0) {
                System.out.println(lines);
                System.out.println("      Login failed, please try again");
                System.out.println(lines);
            }

            System.out.print("\tUsername: ");
            user = System.console().readLine();
            System.out.print("\tPassword: ");
            password = new String(System.console().readPassword());

            base64 = Base64.getEncoder().encodeToString((user + ":" + password)
                .getBytes());

            try {
                if (!twoFactor) {
                    success = StudentSubmission.testAuth(base64);
                }
            } catch (TwoFactorAuthException ex) {
                twoFactor = true;
                System.out.println("two factor exception");
            }

            if (twoFactor) {
                System.out.println("\tTwo-Factor Code:");
                code = System.console().readLine();
                success = StudentSubmission.testTwoFactorAuth(base64, code);
            }
        } while (!success);
        System.out.println("------------------------------------------");
        System.out.println("            Authenticated!");
        System.out.println("------------------------------------------");

        if (appendUserName) {
            repoName += "-" + user;
        }
        return new StudentSubmission(user, base64, repoName, code);
    }

    private static Config processConfig(String file) throws Exception {
        return new Gson().fromJson(new Scanner(new File(file))
            .useDelimiter("\\A").next(), Config.class);
    }

    private static class Config {
        public String[] collaborators;
        public String commitMsg;
        public String repoName;
        public boolean appendUsername;
    }

    public static void main(String... args) throws Exception {
        try {
            Config config = processConfig(args[0]);

            System.out.print("Compiling files.....");
            if (!checkCompile(args)) {
                System.out.println("\nYour submission does not compile. "
                    + "Please fix your compile errors before proceeding.");
                System.exit(1);
            }
            System.out.println("...done");

            StudentSubmission sub = authenticateAndCreate(config.repoName,
                config.appendUsername);
            sub.createRepo();

            for (String collab : config.collaborators) {
                sub.addCollab(collab);
            }

            String fileString = "";
            for (int i = 1; i < args.length; i++) {
                fileString += args[i] + " ";
            }

            String[] fileNames = fileString.replaceAll("'", "").split(" ");

            boolean success = false;
            try {
                success = sub.pushFiles(config.commitMsg, fileNames);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (success) {
                System.out.println("Code submitted successfully!");
                System.out.println("Launching browser to view repo...");
                Desktop.getDesktop().browse(new URI(
                    String.format("http://github.gatech.edu/%s/%s",
                        sub.getUser(),
                    sub.getRepo())));
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
