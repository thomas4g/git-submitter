import java.util.List;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.stream.Collectors;

import java.io.File;

import com.google.gson.Gson;

public class Application {
    
    private final static String DEFAULT_CONFIG = "config.json";
    
    private static boolean checkCompile(String[] files) throws Exception {
        List<String> args = Arrays.stream(files).filter(f -> f
            .substring(f.lastIndexOf('.')).equals(".java"))
            .collect(Collectors.toList());
        args.add(0, "javac");
        ProcessBuilder pcb = new ProcessBuilder(args);
        Process p = pcb.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    private static StudentSubmission authenticateAndCreate(String repo)
        throws Exception {
        String user = "", password = "", base64 = "";
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("    Logging in to Georgia Tech servers    ");
        System.out.println("------------------------------------------");
        do {
            if (base64.length() > 0) {
                System.out.println("------------------------------------------");
                System.out.println("      Login failed, please try again      ");
                System.out.println("------------------------------------------");
            }

            System.out.print("\tUsername: ");
            user = System.console().readLine();
            System.out.print("\tPassword: ");
            password = new String(System.console().readPassword());

            base64 = Base64.getEncoder().encodeToString((user + ":" + password)
                .getBytes());
        } while (!StudentSubmission.testAuth(base64));
        System.out.println("------------------------------------------");
        System.out.println("            Authenticated!");
        System.out.println("------------------------------------------");
        return new StudentSubmission(user, base64, repo);
    }

    private static Config processConfig(File f) throws Exception {
         return new Gson().fromJson(new Scanner(f).useDelimiter("\\A").next(), Config.class);
    }

    private static class Config {
        public String submissionName;
        public String[] collaborators;
        public String commitMsg;
        public String repo;
    }
    public static void main(String... args) throws Exception {

        Config config = processConfig(new File(DEFAULT_CONFIG));

        System.out.print("Compiling files.....");
        if (!checkCompile(args)) {
            System.out.println("\nYour submission does not compile. "
                + "Please fix your compile errors before proceeding.");
            System.exit(1);
        }
        System.out.println("...done");

        //TODO checkstyle option
        //TODO checksum/signature inside zip

        String[] fileNames = args; //change
        System.out.print("Zipping files.......");
        File submissionZip = Utils.zipFiles(config.submissionName + ".zip",
            Arrays.stream(fileNames).map(File::new).toArray(File[]::new));
        System.out.println("...done");

        StudentSubmission sub = authenticateAndCreate(config.repo);
        for (String s : config.collaborators) sub.addCollab(s);

        if (sub.pushFile(submissionZip, config.commitMsg))
            System.out.println("Code submitted successfully!");
        else
            System.out.println("Submission unsuccessful. :(");
    }

}
