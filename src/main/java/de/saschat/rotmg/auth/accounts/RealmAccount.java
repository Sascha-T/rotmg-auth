package de.saschat.rotmg.auth.accounts;

import com.google.gson.reflect.TypeToken;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.exceptions.RequestException;
import de.saschat.rotmg.auth.exceptions.XMLException;
import de.saschat.rotmg.auth.util.CachingSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public abstract class RealmAccount {
    private final String cacheId;
    protected RealmAccount(String cacheId) {
        this.cacheId = cacheId;
        if (CachingSettings.CURRENT != null) {
            /* Load from cache... */
            CachingSettings.CURRENT.folder().mkdirs();
            File cacheFile = new File(CachingSettings.CURRENT.folder(), cacheId);
            if (!cacheFile.exists())
                return;
            try {
                // trust me bro
                verifyData = (List<VerifyData>) CachingSettings.GSON.fromJson(new FileReader(cacheFile), TypeToken.getParameterized(LinkedList.class, VerifyData.class));
            } catch (FileNotFoundException e) {
            }
        }
    }

    private void $cache() {

        if (CachingSettings.CURRENT != null) {
            /* Load from cache... */
            File cacheFile = new File(CachingSettings.CURRENT.folder(), cacheId);

            try {
                FileWriter writer = new FileWriter(cacheFile);
                CachingSettings.GSON.toJson(verifyData, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    protected List<VerifyData> verifyData = new LinkedList();

    private void $cull() {
        $cache();
        if (verifyData.size() > 10) verifyData.remove(9);
    }

    public final VerifyData getVerification() throws RequestException, XMLException, LoginException {
        if (verifyData.isEmpty()) {
            VerifyData data = getFreshVerification();
            verifyData.add(data);
            $cull();
            return data;
        } else {
            VerifyData data = verifyData.get(0);
            if (data.verificationTokenExpiration().before(Date.from(Instant.now()))) {
                // expired...
                data = getFreshVerification();
                verifyData.add(data);
                $cull();
                return data;
            } else {
                return data;
            }
        }
    }

    private static Node $xml(Document doc, String tag) throws XMLException {
        NodeList x = doc.getElementsByTagName(tag);
        if (x.getLength() == 0)
            throw new XMLException("parse: " + tag + " not found.");
        return x.item(0);
    }

    public void clearCache() {
        verifyData.clear();
    }
    protected final VerifyData getFreshVerification() throws RequestException, XMLException, LoginException {
        try {
            String data = getRawVerification();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))); // java stdlib will make me blow my brains out someday... (too lazy to get good)

            NodeList errors = doc.getElementsByTagName("Error");
            if (errors.getLength() != 0) {
                throw new LoginException(errors.item(0).getTextContent());
            }

            int credits = Integer.parseInt($xml(doc, "Credits").getTextContent());
            String accountId = $xml(doc, "AccountId").getTextContent();
            String name = $xml(doc, "Name").getTextContent();

            long creation = Long.parseLong($xml(doc, "CreationTimestamp").getTextContent());
            long muted = Long.parseLong($xml(doc, "MutedUntil").getTextContent());
            String lastLogin = $xml(doc, "LastLogin").getTextContent();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date loginDate = formatter.parse(lastLogin);
            Date mutedDate = new Date(muted * 1000);
            Date creationDate = new Date(creation * 1000);

            String accessToken = $xml(doc, "AccessToken").getTextContent();
            Date verificationTokenTimestamp = new Date(Long.parseLong($xml(doc, "AccessTokenTimestamp").getTextContent()) * 1000);
            Date verificationTokenExpiration = new Date(
                verificationTokenTimestamp.getTime() + Long.parseLong($xml(doc, "AccessTokenExpiration").getTextContent()) * 1000
            );

            return new VerifyData(
                credits,
                accountId,
                creationDate,
                loginDate,
                mutedDate,
                name,

                accessToken,
                verificationTokenTimestamp,
                verificationTokenExpiration
            );
        } catch (ParserConfigurationException | IOException | SAXException | ParseException e) {
            throw new XMLException(e.getMessage());
        }

    }

    public String getToken() throws XMLException, LoginException, RequestException {
        return getVerification().verificationToken();
    }

    public Date getCreation() throws XMLException, LoginException, RequestException {

        return getVerification().verificationTokenTimestamp();
    }

    public Date getExpiration() throws XMLException, LoginException, RequestException {

        return getVerification().verificationTokenExpiration();
    }

    public abstract String getRawVerification() throws RequestException, LoginException, XMLException;

}
