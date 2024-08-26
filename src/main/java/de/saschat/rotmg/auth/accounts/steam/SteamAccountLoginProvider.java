package de.saschat.rotmg.auth.accounts.steam;

import de.saschat.rotmg.auth.exceptions.LoginException;

public interface SteamAccountLoginProvider {
    String getAuthSessionTicket() throws LoginException;
    String getSteamID() throws LoginException;
}
