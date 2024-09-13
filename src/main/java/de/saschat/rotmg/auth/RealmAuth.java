package de.saschat.rotmg.auth;

import de.saschat.rotmg.auth.accounts.deca.DecaAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccountLoginProvider;
import de.saschat.rotmg.auth.cache.CacheProcessor;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.exceptions.RequestException;
import de.saschat.rotmg.auth.util.WMIC;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class RealmAuth {
    private static byte[] random = new byte[0];
    public static String getClientToken() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            StringBuilder text = new StringBuilder();
            text.append(WMIC.getValue("baseboard", "SerialNumber"));
            text.append(WMIC.getValue("path Win32_BIOS", "SerialNumber"));
            if (text.isEmpty())
                text.append("None0");
            text.append(WMIC.getValue("path Win32_OperatingSystem", "SerialNumber"));

            System.out.println(text);

            byte[] digest = md.digest(text.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for(byte a : digest)
                hash.append(String.format("%02x", a));

            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public static DecaAccount login(String username, String password) throws RequestException {
        return new DecaAccount(username, password);
    }
    public static SteamAccount login(SteamAccountLoginProvider provider) throws LoginException {
        return new SteamAccount(provider);
    }
    public static void setCachingSettings(CacheProcessor settings) {
        CacheProcessor.CURRENT = settings;
    }
}
