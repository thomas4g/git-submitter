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

    // private static Config processConfig(String file) throws Exception {
    //     return new Gson().fromJson(new Scanner(new File(file))
    //         .useDelimiter("\\A").next(), Config.class);
    // }

    // private static class Config {
    //     public String[] collaborators;
    //     public String commitMsg;
    //     public String repoName;
    //     public boolean appendUsername;
    // }

    public static void main(String... args) throws Exception {
        // try {

        //     // Temporary
        //     String[] collaborators = {"cs1331"};
        //     String commitMsg = "Some commit messge.";
        //     String repoName = "test-repo";

        //     System.out.print("Compiling files.....");
        //     if (!checkCompile(args)) {
        //         System.out.println("\nYour submission does not compile. "
        //             + "Please fix your compile errors before proceeding.");
        //         System.exit(1);
        //     }
        //     System.out.println("...done");

        //     StudentSubmission sub = authenticateAndCreate(repoName);
        //     sub.createRepo();

        //     for (String collab : collaborators) {
        //         sub.addCollab(collab);
        //     }

        //     String fileString = "";
        //     for (int i = 1; i < args.length; i++) {
        //         fileString += args[i] + " ";
        //     }

        //     String[] fileNames = fileString.replaceAll("'", "").split(" ");

        //     boolean success = false;
        //     try {
        //         success = sub.pushFiles(commitMsg, fileNames);
        //     } catch (Exception e) {
        //         throw new RuntimeException(e.getMessage(), e);
        //     }
        //     if (success) {
        //         System.out.println("Code submitted successfully!");
        //         System.out.println("Launching browser to view repo...");
        //         Desktop.getDesktop().browse(new URI(
        //             String.format("http://github.gatech.edu/%s/%s",
        //                 sub.getUser(),
        //             sub.getRepo())));
        //     } else {
        //         System.out.println("Submission unsuccessful. :(");
        //     }
        // } catch (UnknownHostException ex) {
        //     System.out.println("Internet connection failed. Please repair your"
        //         + " connection and try again.");
        // } catch (Exception e) {
        //     System.out.println("Something went wrong. Please check your"
        //         + " internet connection and that the source files are present."
        //         + " Call a TA for help if you need further assistance.");

        //     System.out.println(e.getMessage());
        //     e.printStackTrace();
        // }
    }

}
