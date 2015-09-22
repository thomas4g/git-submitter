package org.cs1331.gitsubmitter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class Utils {
    private static final String BASE = "https://github.gatech.edu/api/v3/";
    private static final Logger logger = Logger.getLogger(
            Utils.class.getName());
    private static final PrintStream DEBUG = System.out;
    private static final int AES_KEY_SIZE = 128;

    public static File zipFiles(String name, File... files)
        throws IOException {
        File zip = new File(name);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));

        for (File f : files) {
            out.putNextEntry(new ZipEntry(f.getName()));
            byte[] data = readFile(f);
            out.write(data, 0, data.length);
            out.closeEntry();
        }

        out.close();
        return zip;
    }

    public static int doRequest(String path, String type, String auth,
          String content, StringBuilder sb, Map<String, String> headers,
          Map<String, List<String>> responseHeadersOut)
          throws IOException, MalformedURLException {

        HttpsURLConnection conn = (HttpsURLConnection) new URL(BASE + path)
            .openConnection();
        conn.setInstanceFollowRedirects(false);

        try {
            conn.setRequestMethod(type);
            conn.setRequestProperty("Authorization", "Basic " + auth);
            if (null != headers) {
                for (Entry<String, String> header : headers.entrySet()) {
                    conn.setRequestProperty(header.getKey(), header.getValue());
                }
            }

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
            if (null == sb) {
                sb = new StringBuilder();
            }
            if (conn.getInputStream().available() > 0) {
                sb.append(new Scanner(conn.getInputStream()).useDelimiter("\\A")
                .next());
            }
            logger.info("Response code: " + conn.getResponseCode()); 
            logger.info("Response data: ");
            logger.info(sb.toString());

            if (null != responseHeadersOut) {
                responseHeadersOut.putAll(conn.getHeaderFields());
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return conn.getResponseCode();
    }

    public static boolean testAuth(String base64)
        throws TwoFactorAuthException, MalformedURLException, IOException {
        Map<String, List<String>> headers = new HashMap<>();
        int code = doRequest("", "GET", base64, "", null, null, headers);
        if (null != headers.get("X-GitHub-OTP")) {
            throw new TwoFactorAuthException();
        }
        return code != 401;
    }

    public static boolean testTwoFactorAuth(String base64, String code)
        throws IOException, MalformedURLException {
        Map<String, String> auth = new HashMap<>();
        auth.put("X-GitHub-OTP", code);
        return doRequest("", "GET", base64, "", null, auth, null) != 401;
    }

    private static byte[] readFile(String s) throws IOException {
        return readFile(new File(s));
    }
    private static byte[] readFile(File f) throws IOException {
        byte[] bytes = new byte[(int) f.length()];
        new DataInputStream(new FileInputStream(f)).readFully(bytes);
        return bytes;
    }

    private static void saveAESKey(byte[] aesKey, File out, String pubKey)
        throws IOException {
        Cipher rsaCipher = null;

        try {
            rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(readFile(pubKey))));
        } catch (Exception ex) {
            throw new IOException("Something wrong with the public keyfile");
        }

        CipherOutputStream cos = new CipherOutputStream(
            new FileOutputStream(out), rsaCipher);
        cos.write(aesKey);
        cos.close();
    }

    private static byte[] loadAESKey(File file, String privateKey)
        throws IOException {
        Cipher rsaCipher = null;

        try {
            rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA")
                .generatePrivate(
                 new PKCS8EncodedKeySpec(readFile(privateKey))));
        } catch (Exception ex) {
            throw new IOException("Something wrong with the private keyfile");
        }

        byte[] outBytes = new byte[AES_KEY_SIZE / 8];

        CipherInputStream cos = new CipherInputStream(
            new FileInputStream(file), rsaCipher);
        cos.read(outBytes);
        cos.close();
        return outBytes;
    }

    private static void encrypt(File file, File out, File aesOut,
        String pubKey) throws IOException, InvalidKeyException {

        Cipher aesCipher = null;
        try {
            aesCipher = Cipher.getInstance("AES");
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(AES_KEY_SIZE);
            byte[] aesKey = gen.generateKey().getEncoded();

            DEBUG.println("Saving AES Key");
            saveAESKey(aesKey, aesOut, pubKey);
            DEBUG.println("Creating Spec for AES Key");
            SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
            DEBUG.println("Initializing Cipher");
            aesCipher.init(Cipher.ENCRYPT_MODE,
                aesKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
            | InvalidKeyException ex) {
            throw new InvalidKeyException("Problem creating the AES key", ex);
        }

        CipherOutputStream cos = new CipherOutputStream(
            new FileOutputStream(out), aesCipher);
        cos.write(readFile(file));
        cos.close();
    }

    private static void decrypt(File file, File out, File aesIn,
        String privateKey) throws IOException {

        Cipher aesCipher = null;
        try {
            aesCipher = Cipher.getInstance("AES");

            DEBUG.println("Loading AES Key");
            byte[] aesKey = loadAESKey(aesIn, privateKey);

            aesCipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(aesKey, "AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
            | InvalidKeyException ex) {
            throw new IOException("Problem loading the AES key");
        }

        CipherInputStream cos = new CipherInputStream(
            new FileInputStream(file), aesCipher);

        int c;
        FileOutputStream fos = new FileOutputStream(out);
        while ((c = cos.read()) != -1) {
            fos.write(c);
        }
        fos.close();
        cos.close();
    }

    /**
     * Attempts to retrieve a unique identifier for this computer.
     * The MAC address of the currently active network interface is used
     * as this identifier. If the MAC address is unable to be retrieved,
     * "000000000000" is returned.
     * 
     * @return A unique identifier of this computer
     */
    public static String getIdentifier() {
        byte[] id;
        try {
            id = NetworkInterface
                .getByInetAddress(InetAddress.getLocalHost())
                .getHardwareAddress();
        } catch (IOException e) {
            id = null;
            // id will already be null if an exception happens, just being safe
        }
        if (null == id) {
            id = new byte[]{(byte)0, (byte)0, (byte)0,
                    (byte)0, (byte)0, (byte)0};
        }
        return String.format("%02X%02X%02X%02X%02X%02X",
                id[0], id[1], id[2], id[3], id[4], id[5]);
    }

    public static void main(String... args)
        throws IOException, InvalidKeyException {

        if (args[0].equals("help") || args[0].equals("-h")) {
            System.out.println("Encrypt:\n java Bundler public_key File1 File2"
                + " File3");
            System.out.println("Decrypt:\n java Bundler -d private_key File "
                + " encrypted_aes_key");
        } else if (args[0].equals("-d")) {
            decrypt(new File(args[2]), new File("decrypted-" + args[1]),
                new File(args[3]), args[1]);
        } else {
            File zip = zipFiles("files.zip", Arrays.stream(
                Arrays.copyOfRange(args, 1, args.length))
                .map(File::new).toArray(File[]::new));
            File aes = new File("encrypted-aes.der");
            encrypt(zip, new File("encrypted-files.zip"),
                aes, "public_key.der");

            File submissionZip = zipFiles("submission.zip", zip, aes);
        }
    }


}
