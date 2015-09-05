package org.cs1331.gitsubmitter;

import java.util.Scanner;
import java.io.File;

public class Download {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("java Download repoSuffix owner students.txt");
            System.exit(0);
        }
        AuthenticatedUser authenticatedUser = AuthenticatedUser.create();

        String repoSuffix = args[0];
        String owner = args[1];
        Scanner s = new Scanner(new File(args[2]));
        while(s.hasNext()) {
            // WTH is u?
            String u = s.nextLine();
            System.out.println(u);
            StudentSubmission sub = new StudentSubmission(authenticatedUser,
                                                          u + repoSuffix);
            try {
                sub.download(args[3] + "/" + u);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
