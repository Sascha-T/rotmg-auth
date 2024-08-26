package de.saschat.rotmg.auth.javasteam;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.saschat.rotmg.auth.RealmAuth;
import de.saschat.rotmg.auth.accounts.steam.SteamAccount;
import de.saschat.rotmg.auth.accounts.steam.SteamAccountLoginProvider;
import de.saschat.rotmg.auth.exceptions.LoginException;
import de.saschat.rotmg.auth.javasteam.strategies.PasswordLoginStrategy;
import de.saschat.rotmg.auth.util.CachingSettings;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.AppOwnershipTicketCallback;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.GameConnectTokensCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.types.JobID;

import java.io.File;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

public class JavaSteamLoginProvider extends ClientMsgHandler implements SteamAccountLoginProvider {
    SteamClient steamClient = new SteamClient();
    CallbackManager callbackManager = new CallbackManager(steamClient);

    List<byte[]> GAME_CONNECT_TOKENS = new LinkedList<>();
    AtomicBoolean state_gameTokensReceived = new AtomicBoolean(false);
    AtomicBoolean state_tokenAuthorized = new AtomicBoolean(false);
    String user_steamId;
    InetAddress user_ipAddress;
    long user_logOnTime;
    String user_authToken;

    static final int hSteamPipe = new Random().nextInt(1000000) + 1;
    public JavaSteamLoginProvider(SteamLoginStrategy strategy, int timeoutSeconds) throws LoginException, InvalidProtocolBufferException {
        callbackManager.subscribe(LoggedOnCallback.class, this::loggedOn);
        callbackManager.subscribe(GameConnectTokensCallback.class, this::gcTokens);
        callbackManager.subscribe(ConnectedCallback.class, (c) -> {
            strategy.onConnected(steamClient, c);
        });

        steamClient.addHandler(this);
        steamClient.connect();

        int elapsedTime = 0;
        while(elapsedTime != timeoutSeconds) {
            callbackManager.runWaitCallbacks(1000);
            elapsedTime++;

            if(state_gameTokensReceived.get())
                break;
        }

        if(elapsedTime == timeoutSeconds)
            throw new LoginException("timeout elapsed");

        /*steamClient.send(
            SteammessagesClientserver.CMsgClientGetAppOwnershipTicket.newBuilder().build()
        );*/
        SteamApps steamApps = steamClient.getHandler(SteamApps.class);
        AppOwnershipTicketCallback appOwnershipTicketCb = steamApps.getAppOwnershipTicket(200210).runBlock();

        byte[] gameConnectToken = GAME_CONNECT_TOKENS.removeFirst();
        byte[] appOwnershipTicket = appOwnershipTicketCb.getTicket();


        ByteBuffer authTicketBuffer = ByteBuffer.allocate(4 + gameConnectToken.length + 4 + 24 + 4 + appOwnershipTicket.length);
        authTicketBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] unflippedIP = user_ipAddress.getAddress();
        byte[] flippedIP = new byte[] { // @todo: endianess?
            unflippedIP[3],
            unflippedIP[2],
            unflippedIP[1],
            unflippedIP[0]
        };

        authTicketBuffer.putInt(gameConnectToken.length);
        authTicketBuffer.put(gameConnectToken);
        authTicketBuffer.putInt(24);
        authTicketBuffer.putInt(1);
        authTicketBuffer.putInt(2);
        authTicketBuffer.put(flippedIP);
        authTicketBuffer.putInt(0);
        authTicketBuffer.putInt((int) (Instant.now().toEpochMilli() - user_logOnTime));
        authTicketBuffer.putInt(1);
        authTicketBuffer.putInt(appOwnershipTicket.length);
        authTicketBuffer.put(appOwnershipTicket);

        authTicketBuffer.flip();
        byte[] authSessionTicket = authTicketBuffer.array();

        ClientMsgProtobuf<SteammessagesClientserver.CMsgClientAuthList.Builder> authListUpload = new ClientMsgProtobuf(SteammessagesClientserver.CMsgClientAuthList.class, EMsg.ClientAuthList);
        JobID jobID = steamClient.getNextJobID();
        authListUpload.setSourceJobID(jobID);

        authListUpload.getBody().setTokensLeft(GAME_CONNECT_TOKENS.size());
        authListUpload.getBody().setLastRequestSeq(0);
        authListUpload.getBody().setLastRequestSeqFromServer(0);
        authListUpload.getBody().setMessageSequence(0);
        authListUpload.getBody().addAppIds(200210);

        StringBuilder builder = new StringBuilder();
        for (byte b : authSessionTicket)
            builder.append(String.format("%02x", b));

        byte[] sessionTicket = new byte[52];
        System.arraycopy(authSessionTicket, 0, sessionTicket, 0, 52);

        CRC32 ticketCrc = new CRC32();
        ticketCrc.update(sessionTicket);

        authListUpload.getBody().addTickets(SteammessagesBase.CMsgAuthTicket.newBuilder()
            .setEstate(0)
            .setSteamid(0)
            .setGameid(200210)
            .setHSteamPipe(hSteamPipe)
            .setTicketCrc((int) ticketCrc.getValue())
            .setTicket(ByteString.copyFrom(sessionTicket)));

        steamClient.send(authListUpload);

        user_authToken = builder.toString();

        while(true) {
            callbackManager.runWaitCallbacks(1000);

            if(state_tokenAuthorized.get())
                break;
        }

        steamClient.disconnect();
    }


    private void gcTokens(GameConnectTokensCallback gameConnectTokensCallback) {
        GAME_CONNECT_TOKENS.clear();
        GAME_CONNECT_TOKENS.addAll(gameConnectTokensCallback.getTokens());
        state_gameTokensReceived.set(true);
    }

    private void loggedOn(LoggedOnCallback loggedOnCallback) {
        user_ipAddress = loggedOnCallback.getPublicIP();
        user_logOnTime = Instant.now().toEpochMilli();
        user_steamId = Long.toString(loggedOnCallback.getClientSteamID().convertToUInt64());
    }

    @Override
    public String getAuthSessionTicket() throws LoginException {
        return user_authToken;
    }

    @Override
    public String getSteamID() throws LoginException {
        return user_steamId;
    }
    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if(packetMsg.getMsgType() == EMsg.ClientAuthListAck) {
            ClientMsgProtobuf<SteammessagesClientserver.CMsgClientAuthListAck.Builder> ticketResponse =
                new ClientMsgProtobuf<>(SteammessagesClientserver.CMsgClientAuthListAck.class, packetMsg);
            state_tokenAuthorized.set(true);
        }
    }
}
