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

            String text = "";
            text += WMIC.getValue("path Win32_BIOS", "SerialNumber");
            text += WMIC.getValue("baseboard", "SerialNumber");
            if (text.equals(""))
                text += "None0";
            text += WMIC.getValue("Win32_OperatingSystem", "SerialNumber");

            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            String hash = "";
            for(byte a : digest)
                hash += String.format("%02x", a);

            return hash;
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
