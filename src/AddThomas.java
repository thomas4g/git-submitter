import java.net.URL;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;

public class AddThomas {
    public static void main(String[] args) throws Exception {
        System.out.print("Username: ");
        String user = System.console().readLine();
        System.out.print("Password: ");
        String password = new String(System.console().readPassword());
        //yes the colon
        String auth = java.util.Base64.getEncoder().encodeToString((
            user + ":" + password).getBytes());
        
        String repoSuffix = args[0];
        Scanner s = new Scanner(new File(args[1]));
        while(s.hasNext()) {
            String u = s.nextLine();
            System.out.println(u);
            StudentSubmission sub = new StudentSubmission(user, auth,  u + repoSuffix);
/*            try {
                sub.download(u, user);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }*/
            sub.addCollab("tshields3");
        }
    }
}

