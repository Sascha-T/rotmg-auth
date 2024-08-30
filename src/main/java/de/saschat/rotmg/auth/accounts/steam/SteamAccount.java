package de.saschat.rotmg.auth.accounts.steam;

import de.saschat.rotmg.auth.RealmAuth;
import de.saschat.rotmg.auth.accounts.RealmAccount;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.exceptions.RequestException;
import de.saschat.rotmg.auth.exceptions.XMLException;
import de.saschat.rotmg.auth.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class SteamAccount extends RealmAccount {

    SteamAccountLoginProvider provider;
    public SteamAccount(SteamAccountLoginProvider steam) throws LoginException {
        super(getCacheIdentifier(steam.getSteamID()));
        this.provider = steam;
    }

    @Override
    public String getGUID() throws LoginException {
        return "steamworks:" + provider.getSteamID();
    }

    private static Node $xml(Document doc, String tag) throws XMLException {
        NodeList x = doc.getElementsByTagName(tag);
        if (x.getLength() == 0)
            throw new XMLException("parse: " + tag + " not found.");
        return x.item(0);
    }

    private String getAccessToken() throws LoginException, RequestException, XMLException {
        String loginData = String.format("sessionticket=%s&game_net=%s&play_platform=%s&game_net_user_id=%s&steamid=%s",
            provider.getAuthSessionTicket(),
            "Unity_steam",
            "Unity_steam",
            provider.getSteamID(),
            provider.getSteamID()
        );
        try {
            HttpsURLConnection conn = (HttpsURLConnection) (new URL(Constants.LOGIN_STEAM).openConnection());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(loginData.length()));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.getOutputStream().write(loginData.getBytes(StandardCharsets.UTF_8));
            byte[] data = conn.getInputStream().readAllBytes();

            System.out.println(new String(data));

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(data)); // java stdlib will make me blow my brains out someday... (too lazy to get good)

            return $xml(doc, "Secret").getTextContent();
        } catch (IOException e) {
            throw new RequestException(e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            throw new XMLException(e.getMessage());
        }
    }

    @Override
    public String getRawVerification() throws RequestException, LoginException, XMLException {
        String loginData = String.format("guid=%s&steamid=%s&secret=%s&clientToken=%s&game_net=%s&play_platform=%s&game_net_user_id=%s&steamid=%s",
            String.format("steamworks:" + provider.getSteamID()),
            provider.getSteamID(),
            getAccessToken(),
            RealmAuth.getClientToken(),
            "Unity_steam",
            "Unity_steam",
            provider.getSteamID(),
            provider.getSteamID()); // yes steamid twice.. don't ask me, ask rotmg devs... this seems like a ticking time bomb
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

    private static String getCacheIdentifier(String steamId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update("valve".getBytes(StandardCharsets.UTF_8));
            md.update(steamId.getBytes(StandardCharsets.UTF_8));
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
