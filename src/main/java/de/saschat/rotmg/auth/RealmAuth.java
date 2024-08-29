package de.saschat.rotmg.auth;

import de.saschat.rotmg.auth.accounts.deka.DecaAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccountLoginProvider;
import de.saschat.rotmg.auth.cache.CacheProcessor;
import de.saschat.rotmg.auth.cache.FileCacheProcessor;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.exceptions.RequestException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

// @todo: caching
public class RealmAuth {
    private static byte[] random = new byte[0];
    public static String getClientToken() {
        byte[] data = new byte[20];
        try {
            String hwid = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(hwid.getBytes());
            data = md.digest();
        } catch (NoSuchAlgorithmException e) {
            if(random.length == 0) {
                random = new byte[20];
                new Random().nextBytes(random);
            }
            data = random;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < data.length; i++) output.append(String.format("%02x", data[i]));
        return output.toString();
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
