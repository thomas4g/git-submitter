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

    private static String request(String path, String type, String auth,
        String content) throws Exception {

        HttpsURLConnection conn = (HttpsURLConnection) new URL(BASE + path)
            .openConnection();
        try {
            conn.setRequestMethod(type);
            conn.setRequestProperty("Authorization", "Basic " + auth);
            if (content.length() > 0) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.close();
            }
            return new Scanner(conn.getInputStream()).useDelimiter("\\A")
                .next();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println(conn.getResponseMessage());
        }
        return "";
    }

    private static boolean testAuth(String base64)
        throws Exception {

        String resp = request("", "GET", base64, "");

        //TODO: incorporate JSON library and properly check this
        //because this is sososososos hacky
        return resp.indexOf("current_user") != -1;
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
        String resp = request(String.format("repos/%s/%s/contents/%s",
            repoOwner, repo, f.getName()), "PUT", auth, json);

        //TODO
        return true;
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
        pushFile(args[counter], args[counter + 1], new File(args[counter + 2]),
            base64, args[counter + 3]);
    }
}
