package org.cs1331.gitsubmitter;

import java.io.File;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class Forker {
    private static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].contains("-h")) {
            System.out.println("Usage:");
            System.out.println("java Forker repositorySuffix [organization] studentsFile");
            System.out.println("e.g:");
            System.out.println("java Forker -timedlab cs1331-timedlabs b1.txt");
            System.exit(0);
        }
        AuthenticatedUser authenticatedUser = AuthenticatedUser.create();

        String repoSuffix = args[0];
        String org = args[1];
        Scanner s = new Scanner(new File(args[args.length > 2 ? 2 : 1]));
        boolean delete = false;
        for (String s2 : args) {
            if (s2.equals("-d")) delete = true;
        }
        while (s.hasNext()) {
            // WTH is u?
            String u = s.nextLine();
            if (delete) {
                StudentSubmission ss =
                    new StudentSubmission(authenticatedUser, u + repoSuffix);
                System.out.println(u);
                System.out.println(ss.delete());
            } else {
                System.out.println("TODO: Implement me. Not forked!");

                // StudentSubmission ss =
                //     new StudentSubmission(authenticatedUser, u + repoSuffix);
                // System.out.println(u);
                // System.out.println(args.length > 2 ? ss.fork(org) : ss.fork());
                // new StudentSubmission(
                //     args.length > 2 ? org : user, auth, u + repoSuffix)
                //     .removeCollab(u);
            }
        }
    }
}
