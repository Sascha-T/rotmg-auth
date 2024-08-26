package de.saschat.rotmg.auth.accounts.deka;

import de.saschat.rotmg.auth.RealmAuth;
import de.saschat.rotmg.auth.accounts.RealmAccount;
import de.saschat.rotmg.auth.exceptions.RequestException;
import de.saschat.rotmg.auth.util.Constants;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DecaAccount extends RealmAccount {
    public String username;
    private String password;

    public DecaAccount(String username, String password) {
        super(getCacheIdentifier(username, password));
        this.username = username;
        this.password = password;
    }

    @Override
    public String getRawVerification() throws RequestException {
        String loginData = String.format("guid=%s&password=%s&clientToken=%s&game_net=%s&play_platform=%s&game_net_user_id=%s",
            username,
            password,
            RealmAuth.getClientToken(),
            "Unity",
            "Unity",
            "");
        try {
            HttpsURLConnection conn = (HttpsURLConnection) (new URL(Constants.LOGIN).openConnection());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(loginData.length()));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.getOutputStream().write(loginData.getBytes(StandardCharsets.UTF_8));
            byte[] data = conn.getInputStream().readAllBytes();

            return new String(data);
        } catch (IOException e) {
            throw new RequestException(e.getMessage());
        }
    }

    private static String getCacheIdentifier(String username, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(username.getBytes(StandardCharsets.UTF_8));
            md.update("dekadekadeka".getBytes(StandardCharsets.UTF_8));
            md.update(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x88});

            StringBuilder output = new StringBuilder();
            for (byte b : md.digest())
                output.append(String.format("%02X", b));
            return output.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
