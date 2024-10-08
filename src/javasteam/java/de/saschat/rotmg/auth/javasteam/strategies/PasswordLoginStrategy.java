package de.saschat.rotmg.auth.javasteam.strategies;

import de.saschat.rotmg.auth.cache.CacheProcessor;
import de.saschat.rotmg.auth.cache.FileCacheProcessor;
import de.saschat.rotmg.auth.javasteam.SteamLoginStrategy;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordLoginStrategy implements SteamLoginStrategy {
    private final String username;
    private final String password;

    public PasswordLoginStrategy(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void onConnected(SteamClient client, ConnectedCallback steamClient) {
        AuthSessionDetails details = new AuthSessionDetails();
        details.username = username;
        details.password = password;

        details.persistentSession = true;
        details.authenticator = new UserConsoleAuthenticator();

        SteamUnifiedMessages unifiedMessages = client.getHandler(SteamUnifiedMessages.class);
        SteamAuthentication auth = new SteamAuthentication(client, unifiedMessages);
        SteamUser steamUser = client.getHandler(SteamUser.class);

        try {
            String loaded = loadToken();
            if(loaded == null) {
                CredentialsAuthSession authSession = auth.beginAuthSessionViaCredentials(details);

                AuthPollResult pollResponse = authSession.pollingWaitForResultCompat().get();

                saveToken(loaded = (pollResponse.getRefreshToken() + ":" + pollResponse.getAccountName()));
            }
            String[] split = loaded.split(":");

            LogOnDetails dts = new LogOnDetails();
            dts.setUsername(split[1]);
            dts.setAccessToken(split[0]);
            dts.setShouldRememberPassword(true);
            dts.setLoginID(1234);

            steamUser.logOn(dts);
        } catch (Throwable e) {
            client.disconnect();
            throw new RuntimeException(e);
        }

    }

    private void saveToken(String newGuardData) {
        CacheProcessor.storeIfPossible(getCacheIdentifier(username), newGuardData);
    }

    private String loadToken() {
        return CacheProcessor.retrieveIfPossible(getCacheIdentifier(username));
    }

    private static String getCacheIdentifier(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(username.getBytes(StandardCharsets.UTF_8));
            md.update("steamguard".getBytes(StandardCharsets.UTF_8));
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
