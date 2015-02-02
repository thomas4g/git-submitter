import java.util.List;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import java.io.File;

public class Application {
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

    private static void processConfig(File f) {


    public static void main(String... args) throws Exception {
        /* Input Vars, will come from JSON eventually, hard-coded for now */
        String submissionName = "submission.zip";
        String[] collaborators = {"tshields3"};
        String commitMsg = "Autosubmmited at <time> by the 1331 Submission JAR";
        String repo = "cs1331-timed-lab-1-31-15";

        System.out.print("Compiling files.....");
        if (!checkCompile(args)) {
            System.out.println("Your submission does not compile. "
                + "Please fix your compile errors before proceeding.");
            System.exit(1);
        }
        System.out.println("...done");

        //TODO checkstyle option
        //TODO checksum/signature inside zip

        String[] fileNames = args; //change
        System.out.print("Zipping files.......");
        File submissionZip = Utils.zipFiles(submissionName,
            Arrays.stream(fileNames).map(File::new).toArray(File[]::new));
        System.out.println("...donefor (String s : collaborators) sub.addCollab(s);

        if (sub.pushFile(submissionZip, commitMsg))
            System.out.println("Code submitted successfully!");
        else
            System.out.println("Submission unsuccessful. :(");
    }

}
