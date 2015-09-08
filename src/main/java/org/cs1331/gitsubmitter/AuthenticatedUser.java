package org.cs1331.gitsubmitter;

import java.util.Base64;
import java.util.Scanner;

public class AuthenticatedUser {

    public final String name;
    public final String base64Auth;
    public final String twoFactorCode;

    private AuthenticatedUser(String userName,
                              String base64Auth,
                              String twoFactorCode) {
        this.name = userName;
        this.base64Auth = base64Auth;
        this.twoFactorCode = twoFactorCode;
    }

    /**
     * @todo: dependency on user-interaction method should be injected
     */
    public static AuthenticatedUser create()
            throws Exception {
        String user = "", password = "", base64Auth = "";
        String lines = "------------------------------------------";
        System.out.println();
        System.out.println(lines);
        System.out.println("    Logging in to Georgia Tech servers    ");
        System.out.println(lines);

        boolean success = false;
        boolean twoFactor = false;
        String twoFactorCode = null;
        Scanner keyboard = new Scanner(System.in);
        Thread maskPassword = new Thread(() -> {
            while (true) {
                System.out.print("\010\040");
                try {
                    Thread.currentThread().sleep(1);
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });

        do {
            if (base64Auth.length() > 0) {
                System.out.println(lines);
                System.out.println("      Login failed, please try again");
                System.out.println(lines);
            }

            System.out.print("\tUsername: ");
            System.out.flush();
            user = keyboard.nextLine();
            System.out.print("\tPassword: ");
            System.out.flush();
            maskPassword.start();
            password = keyboard.nextLine();
            maskPassword.stop();

            base64Auth = Base64.getEncoder().encodeToString((user + ":" + password)
                .getBytes());

            try {
                if (!twoFactor) {
                    success = Utils.testAuth(base64Auth);
                }
            } catch (TwoFactorAuthException ex) {
                twoFactor = true;
                System.out.println("two factor exception");
            }

            if (twoFactor) {
                System.out.println("\tTwo-Factor Code:");
                twoFactorCode = keyboard.nextLine();
                success = Utils.testTwoFactorAuth(base64Auth, twoFactorCode);
            }
        } while (!success);
        System.out.println("------------------------------------------");
        System.out.println("            Authenticated!");
        System.out.println("------------------------------------------");

        return new AuthenticatedUser(user, base64Auth, twoFactorCode);
    }

}
