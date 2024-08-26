package de.saschat.rotmg.auth.javasteam;

import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;

public interface SteamLoginStrategy {
    void onConnected(SteamClient client, ConnectedCallback steamClient);
}
