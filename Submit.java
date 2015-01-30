import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import java.util.Scanner;
import java.util.Base64;

import java.io.OutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Submit {
    private static final String BASE = "https://github.gatech.edu/api/v3/";

    private static int request(String path, String type, String auth,
        String content) throws Exception {

        HttpsURLConnection conn = (HttpsURLConnection) new URL(BASE + path)
            .openConnection();
        try {
            conn.setRequestMethod(type);
            conn.setRequestProperty("Authorization", "Basic " + auth);
                conn.setDoOutput(true);
            if (content.length() > 0) {
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.close();
            } else {
                conn.setFixedLengthStreamingMode(0); 
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println(conn.getResponseMessage());
        }
        return conn.getResponseCode();
    }

    private static boolean testAuth(String base64)
        throws Exception {
         return request("", "GET", base64, "") != 401;
    }
    
    private static void create(String user, String repo, String auth) throws Exception {
        if(request(String.format("repos/%s/%s", user, repo), "GET", auth, "") == 404) {
            request("user/repos", "POST", auth, 
                String.format("{\"name\":\"%s\",\"private\": true}", repo));
        }
    }

    private static void addCollab(String owner, String repo, String auth, String user) throws Exception {
        String path = String.format("repos/%s/%s/collaborators/%s", owner, repo, user);
        request(path, "PUT", auth, "");
    }

    private static boolean pushFile(String repoOwner, String repo, File f,
        String auth, String message) throws Exception {

        byte[] fileContents = new byte[(int) f.length()];
        new DataInputStream(new FileInputStream(f)).readFully(fileContents);

        String json = String
        //TODO: json may not really be necessary? but add it? idk
            .format("{\"path\":\"%s\",\"message\":\"%s\",\"content\":\"%s\"}",
                f.getName(), message, Base64.getEncoder()
                    .encodeToString(fileContents));
        return 200 == request(String.format("repos/%s/%s/contents/%s",
            repoOwner, repo, f.getName()), "PUT", auth, json);
    }


    public static void main(String... args) throws Exception {
        String user = "", password = "", base64 = "";
        int counter = 0;
        do {
            if (base64.length() > 0)
                System.out.println("Credentials incorrect, please try again");

            if (args.length > 1 && base64.length() == 0
                && args[0].equals("-u")) {
                user = args[1];
                counter += 2;
                if (args.length == 4 && args[2].equals("-p")) {
                    password = args[3];
                    counter += 2;
                } else {
                    System.out.print("Password: ");
                    password = new String(System.console().readPassword());
                }
            } else {
                System.out.print("username: ");
                user = System.console().readLine();
                System.out.print("password: ");
                password = new String(System.console().readPassword());
            }
            base64 = Base64.getEncoder().encodeToString((user + ":" + password)
                .getBytes());
        } while (!testAuth(base64));
        String repo = args[counter++];
        String file = args[counter++];
        String msg = args[counter++];
        String collab = args[counter];
//        create(user, repo, base64);
        addCollab(user, repo, base64, collab);
 //       pushFile(user, repo, new File(file),
  //          base64, msg);
    }
}
