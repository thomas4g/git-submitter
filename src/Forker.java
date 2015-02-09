import java.util.Scanner;
import java.io.File;

public class Forker {
    private static int fork(String user, String repo, String auth) throws Exception {
        return StudentSubmission.doRequest(String.format(
            "repos/%s/%s/forks", user, repo), "POST", auth, 
            "", null, null, null);
    }
    public static void main(String[] args) throws Exception {
        System.out.print("Username: ");
        String user = System.console().readLine();
        System.out.print("Password: ");
        String password = new String(System.console().readPassword());
        //yes the colon
        String auth = java.util.Base64.getEncoder().encodeToString((
            user + ":" + password).getBytes());
        
        Scanner s = new Scanner(new File(args[0]));
        while(s.hasNext()) {
            String u = s.nextLine();
            System.out.println(u);
            System.out.println(fork(u, u + "-timedlab0.5", auth));
        }
    }
}

