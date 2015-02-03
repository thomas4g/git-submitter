import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import java.util.Scanner;
import java.util.Base64;

import java.io.OutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Represents a student's authenticated connection to a particular
 * submission repository.
 * @author Thomas Shields
 * @version 2.0
 */
public class StudentSubmission {
    private static final String BASE = "https://github.gatech.edu/api/v3/";

    private String user;
    private String base64Auth;
    private String repo;

    public StudentSubmission(String u, String b, String r) {
        user = u;
        base64Auth = b;
        repo = r;
        try {
            createRepo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getUser() {
        return user;
    }
    private int request(String path, String type, String content)
        throws Exception {
        return doRequest(path, type, base64Auth, content, null);
    }
    private int request(String path, String type, String content,
        StringBuilder out) throws Exception {
        return doRequest(path, type, base64Auth, content, out);
    }

    public static int doRequest(String path, String type, String auth,
        String content, StringBuilder sb) throws Exception {

        HttpsURLConnection conn = (HttpsURLConnection) new URL(BASE + path)
            .openConnection();
        try {
            conn.setRequestMethod(type);
            conn.setRequestProperty("Authorization", "Basic " + auth);

            if (!type.equals("GET")) {
                conn.setDoOutput(true);
                if (content.length() > 0) {
                    OutputStream os = conn.getOutputStream();
                    os.write(content.getBytes());
                    os.close();
                } else {
                    conn.setFixedLengthStreamingMode(0);
                }
            }
            if (null != sb) {
                sb.append(new Scanner(conn.getInputStream()).useDelimiter("\\A")
                    .next());
            }
        } catch (IOException e) {
            e = e;
        }
        return conn.getResponseCode();
    }

    public static boolean testAuth(String base64)
        throws Exception {
        return doRequest("", "GET", base64, "", null) != 401;
    }

    private void createRepo() throws Exception {
        if (request(String.format("repos/%s/%s", user, repo), "GET", "")
            == 404) {
            request("user/repos", "POST",
                String.format("{\"name\":\"%s\",\"private\": true}", repo));
        }
    }

    /**
     * Adds someone (a TA, for example)
     * as a collaborator
     * @param collab the user name  to add
     * @return whether not the operation succeeded
     */
    public boolean addCollab(String collab) throws Exception {
        String path = String.format("repos/%s/%s/collaborators/%s", user, repo,
            collab);
        return request(path, "PUT", "") == 204;
    }


    /**
     * submits a file to the user's submission repo
     * @param f     the file to submit
     * @param message the commit message
     * @return if succesful
     */
    public boolean pushFile(File f, String message) throws Exception {
        StringBuilder sb = new StringBuilder();
        String sha = "";
        if (request(String.format("repos/%s/%s/contents/%s",
                user, repo, f.getName()), "GET", "", sb) != 404) {
            sha = sb.toString().split("sha\":\"")[1].split("\"")[0];
        }

        byte[] fileContents = new byte[(int) f.length()];
        new DataInputStream(new FileInputStream(f)).readFully(fileContents);

        //TODO: json may not really be necessary? but add it? idk
        String json = String
            .format("{\"path\":\"%s\",\"message\":\"%s\",\"content\":\"%s\","
                + "\"sha\": \"%s\"}", f.getName(), message, Base64.getEncoder()
                    .encodeToString(fileContents), sha);
        int resp = request(String.format("repos/%s/%s/contents/%s",
            user, repo, f.getName()), "PUT", json);
        return resp == 200 || resp == 201;
    }
}
