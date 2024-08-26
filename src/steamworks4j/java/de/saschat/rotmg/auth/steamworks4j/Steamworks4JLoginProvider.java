package de.saschat.rotmg.auth.steamworks4j;

import com.codedisaster.steamworks.*;
import de.saschat.rotmg.auth.RealmAuth;
import de.saschat.rotmg.auth.accounts.steam.SteamAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccountLoginProvider;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.util.CachingSettings;

import java.io.File;
import java.io.FileWriter;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class Steamworks4JLoginProvider implements SteamAccountLoginProvider {
    static {
        try {
            SteamAPI.loadLibraries();
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }
    }
    private static int refCount = 0;
    public Cleaner cleaner = Cleaner.create();


    static boolean initialized = false;
    static boolean initSteam() {
        if(initialized)
            return true;
        try {
            File file = new File("steam_appid.txt");
            file.deleteOnExit();

            FileWriter writer = new FileWriter(file);
            writer.write("200210");
            writer.flush();
            writer.close();

            initialized = SteamAPI.init();

            file.delete();

            return initialized;
        } catch (Throwable e) {
            return false;
        }
    }
    static void exitSteam() {

    }

    public Steamworks4JLoginProvider() throws SteamException {
        if(!initSteam())
            throw new SteamException("steamworks4j unable to initialize.");
        refCount++;
        cleaner.register(this, () -> {
            refCount--;
            if(refCount == 0)
                exitSteam();
        });

        steamUser = new SteamUser(new SteamUserCallback() {
            @Override
            public void onAuthSessionTicket(SteamAuthTicket steamAuthTicket, SteamResult steamResult) {

            }

            @Override
            public void onValidateAuthTicket(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse, SteamID steamID1) {

            }

            @Override
            public void onMicroTxnAuthorization(int i, long l, boolean b) {

            }

            @Override
            public void onEncryptedAppTicket(SteamResult steamResult) {

            }
        });
    }

    private SteamUser steamUser;

    @Override
    public String getAuthSessionTicket() throws LoginException {
        try {
            ByteBuffer direct = ByteBuffer.allocateDirect(2048);
            int[] sizes = new int[1];
            steamUser.getAuthSessionTicket(direct, sizes);
            String str = "";
            for (int i = 0; i < sizes[0]; i++) {
                str += String.format("%02X", direct.get(i));
            }
            return str;
        } catch (SteamException e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public String getSteamID() throws LoginException {
        return String.valueOf((long) steamUser.getSteamID().getAccountID() + 76561197960265728L); // @todo: this sucks.
    }
}
