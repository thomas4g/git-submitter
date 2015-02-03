import java.util.List;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import java.io.File;

import com.google.gson.Gson;

/* Exceptions*/
import java.net.UnknownHostException;

public class Application {

    private static final String DEFAULT_CONFIG = "config.json";

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
        String lines = "------------------------------------------";
        System.out.println();
        System.out.println(lines);
        System.out.println("    Logging in to Georgia Tech servers    ");
        System.out.println(lines);
        do {
            if (base64.length() > 0) {
                System.out.println(lines);
                System.out.println("      Login failed, please try again");
                System.out.println(lines);
            }
            user = JOptionPane.showInputDialog(null, "Username (e.g, gburdell3):");
            JPasswordField pf = new JPasswordField();
            int res = JOptionPane.showConfirmDialog(null, pf, "Password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
            password = new String(pf.getPassword());
            } 
            /*System.out.print("\tUsername: ");
            user = System.console().readLine();
            System.out.print("\tPassword: ");
            password = new String(System.console().readPassword());
                */
            base64 = Base64.getEncoder().encodeToString((user + ":" + password)
                .getBytes());
        } while (!StudentSubmission.testAuth(base64));
        System.out.println("------------------------------------------");
        System.out.println("            Authenticated!");
        System.out.println("------------------------------------------");
        return new StudentSubmission(user, base64, repo);
    }

    private static Config processConfig() throws Exception {
        return new Gson().fromJson(new Scanner(Application.class
            .getResourceAsStream(DEFAULT_CONFIG))
            .useDelimiter("\\A").next(), Config.class);
    }

    private static class Config {
        public String submissionPrefix;
        public String[] collaborators;
        public String commitMsg;
        public String repo;
    }
    public static void main(String... args) throws Exception {
        try {
            Config config = processConfig();

            System.out.print("Compiling files.....");
            if (!checkCompile(args)) {
                System.out.println("\nYour submission does not compile. "
                    + "Please fix your compile errors before proceeding.");
                System.exit(1);
            }
            System.out.println("...done");

            StudentSubmission sub = authenticateAndCreate(config.repo);
            //TODO checkstyle option
            //TODO checksum/signature inside zip

            String[] fileNames = args; //change
            System.out.print("Zipping files.......");
            File submissionZip = Utils.zipFiles(config.submissionPrefix
                + sub.getUser() + ".zip",
                Arrays.stream(fileNames).map(File::new).toArray(File[]::new));
            System.out.println("...done");

            for (String s : config.collaborators) sub.addCollab(s);

            if (sub.pushFile(submissionZip, config.commitMsg))
                System.out.println("Code submitted successfully!");
            else
                System.out.println("Submission unsuccessful. :(");
        } catch (UnknownHostException ex) {
            System.out.println("Internet connection failed. Please repair your"
                + " connection and try again.");
        } catch (Exception e) {
            System.out.println("Something went wrong. Please check your"
                + " internet connection and that the source files are present."
                + " Call a TA for help if you need further assistance.");

            System.out.println(e.getMessage());
        }
    }

}
