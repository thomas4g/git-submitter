import java.util.Scanner;
import java.io.File;

public class Forker {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].contains("-h")) {
            System.out.println("Usage:");
            System.out.println("java Forker repositorySuffix [organization] studentsFile");
            System.out.println("e.g:");
            System.out.println("java Forker -timedlab cs1331-timedlabs b1.txt");
            System.exit(0);
        }
        String user = "";
        String password = "";
        String auth = "";
        do {
            if (!user.equals("")) {
                System.out.println("Login failed, try again: ");
            }
            System.out.print("Username: ");
            user = System.console().readLine();
            System.out.print("Password: ");
            password = new String(System.console().readPassword());
            //yes the colon
            auth = java.util.Base64.getEncoder().encodeToString((
                user + ":" + password).getBytes());
        } while (!StudentSubmission.testAuth(auth));

        String repoSuffix = args[0];
        String org = args[1];
        Scanner s = new Scanner(new File(args[args.length > 2 ? 2 : 1]));
        while(s.hasNext()) {
            String u = s.nextLine();
            StudentSubmission ss = 
                new StudentSubmission(u, auth, u + repoSuffix);
            System.out.println(u);
            System.out.println(args.length > 2 ? ss.fork(org) : ss.fork());
            new StudentSubmission(
                args.length > 2 ? org : user, auth, u + repoSuffix)
                .removeCollab(u);
        }
    }
}
