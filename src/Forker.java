public class Forker {
    private static void fork(String user, String repo, String auth) throws Exception {
        StudentSubmission.doRequest(String.format(
            "repos/%s/%s/forks", user, repo), "POST", auth, 
            "{\"organization\": \"cs1331\"}", null);
    }
    public static void main(String[] users) throws Exception {
        String auth = java.util.Base64.getEncoder().encodeToString(("tshields3:"
            + new String(System.console().readPassword())).getBytes());
        for(String u : users) fork(u, "test", auth);
    }
}

