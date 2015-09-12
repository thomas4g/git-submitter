package org.cs1331.gitsubmitter;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;

/**
 * Represents a student's authenticated connection to a particular
 * submission repository.
 * @author Thomas Shields
 */
public class StudentSubmission {
    private static final String BASE = "https://github.gatech.edu/api/v3/";

    private String user;
    private String base64Auth;
    private String repo;
    private Map<String, String> headers;
    private Gson gson;

    public StudentSubmission(AuthenticatedUser authenticatedUser,
                             String repoName) {
        user = authenticatedUser.name;
        base64Auth = authenticatedUser.base64Auth;
        repo = repoName;
        headers = new HashMap<>();
        if (null != authenticatedUser.twoFactorCode) {
            headers.put("X-GitHub-OTP", authenticatedUser.twoFactorCode);
        }
        gson = new Gson();
    }

    public String getUser() {
        return user;
    }

    public String getRepo() {
        return repo;
    }

    private int request(String path, String type, String content)
            throws Exception {
        return Utils.doRequest(path, type, base64Auth, content, null,
                               headers, null);
    }

    private int request(String path, String type, String content,
        StringBuilder out) throws Exception {
        return Utils.doRequest(path, type, base64Auth, content, out,
                               headers, null);
    }

    private int request(String path, String type, String content,
        Map<String, List<String>> respHeadersOut) throws Exception {
        return Utils.doRequest(path, type, base64Auth, content, null,
                               headers, respHeadersOut);
    }

    private int request(String path, String type, String content,
        StringBuilder out, Map<String, List<String>> respHeadersOut)
        throws Exception {
        return Utils.doRequest(path, type, base64Auth, content, out,
                               headers, respHeadersOut);
    }


    public void createRepo() throws Exception {
        if (request(String.format("repos/%s/%s", user, repo), "GET", "")
            == 404) {
            request("user/repos", "POST",
                String.format("{\"name\":\"%s\",\"private\": true,"
                    + "\"auto_init\": true}", repo));
        }
    }

    /**
     * Downloads the repo into the specified zip file
     * @param fileName the base name of the zip file to save into
     */
    public void download(String fileName)
        throws Exception {
        Map<String, List<String>> respHeaders = new HashMap<>();
        int code = request(String.format(
            "repos/%s/%s/zipball", user, repo), "GET", "", respHeaders);
        if (code == 404) {
            throw new Exception("404 Not Found");
        }
        try (
            BufferedInputStream bis = new BufferedInputStream(new URL(
                respHeaders.get("Location").get(0)).openStream());
            FileOutputStream fos = new FileOutputStream(
                new File(fileName + ".zip"));
        ) {
            final byte[] data = new byte[1024];
            int count;
            while ((count = bis.read(data, 0, 1024)) != -1) {
                fos.write(data, 0, count);
            }
        } catch (Exception e) {
            throw new Exception("Encountered error while downloading "
                + user + "'s submission", e);
        }
    }

    /**
     * Forks the repo using the StudentSubmissions' credentials
     * E.g, you can use someone else's auth for the submission
     * and then use it to fork
     */
    public int fork() throws Exception {
        return request(String.format(
            "repos/%s/%s/forks", user, repo), "POST", "");
    }

    public int fork(String org) throws Exception {
        StringBuilder sb = new StringBuilder();
        return request(String.format(
            "repos/%s/%s/forks?organization=%s", user, repo, org),
            "POST", "", sb);
    }

    public int delete() throws Exception {
        return request(String.format(
            "repos/%s/%s", user, repo), "DELETE", "");
    }

    public boolean removeCollab(String collab) throws Exception {
        return request(String.format(
            "repos/%s/%s/collaborators/%s", user, repo, collab), "DELETE", "")
            == 204;
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

    private void expandFiles(File file, List<File> list) {
        if (file.isDirectory()) {
            Arrays.stream(file.listFiles()).forEach(f -> expandFiles(f, list));
        } else {
            list.add(file);
        }
    }

    private boolean checkResponse(int code) {
        return code >= 200 && code < 300;
    }

    private SHAObject createCommit(Commit commit) throws Exception {
        StringBuilder sb = new StringBuilder();
        request(String.format("repos/%s/%s/git/commits",
            user, repo), "POST", gson.toJson(commit), sb);
        return gson.fromJson(sb.toString(),
            SHAObject.class);
    }

    private Ref getHeadRef() throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean success = checkResponse(request(
            String.format("repos/%s/%s/git/refs/heads/master", user, repo),
            "GET", null, sb));
        return gson.fromJson(sb.toString(), Ref.class);
    }

    public boolean pushFiles(String message, String ... fileNames)
            throws Exception {
        SHAObject tree = createTree(null, fileNames);
        Ref head = getHeadRef();
        SHAObject newRef = createCommit(new Commit(message, tree.sha,
            head.object.sha));

        return checkResponse(request(
            String.format("repos/%s/%s/git/refs/heads/master", user, repo),
            "PUT", gson.toJson(newRef)));
    }

    private SHAObject createTree(String baseTree, String... fileNames)
            throws Exception {
        TreeRoot tree = new TreeRoot();
        tree.base_tree = baseTree;

        List<File> files = new ArrayList<>();
        Arrays.stream(fileNames).map(File::new)
            .forEach(f -> expandFiles(f, files));

        tree.tree = files.stream().filter(f -> !f.isDirectory()).map(Tree::new)
            .toArray(Tree[]::new);

        StringBuilder sb = new StringBuilder();
        request(String.format("repos/%s/%s/git/trees", user,
            repo), "POST", gson.toJson(tree), sb);
        return gson.fromJson(sb.toString(), SHAObject.class);
    }

    private class Ref {
        public SHAObject object;
    }

    private class SHAObject {
        public String sha;
    }

    private class TreeRoot {
        public String base_tree;
        public Tree[] tree;
    }

    private class Tree {
        public static final String FILE_MODE = "100644";
        public static final String TYPE_FILE = "blob";
        public String path;
        public String mode;
        public String type;
        public String content;
        public Tree(File f) {
            path = f.getPath().replace(File.separatorChar, '/');
            this.mode = FILE_MODE;
            this.type = TYPE_FILE;
            byte[] fileContents = new byte[(int) f.length()];
            try {
                new DataInputStream(new FileInputStream(f))
                    .readFully(fileContents);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            content = new String(fileContents);
        }
    }

    private class Commit {
        public String message;
        public String tree;
        public String[] parents;

        public Commit(String message, String tree, String... parents) {
            this.message = message;
            this.tree = tree;
            this.parents = parents;
        }
    }


}
