package de.saschat.rotmg.auth.accounts;

import java.util.Date;

public record VerifyData(
    int credits, // funny p2w coins
    String accountId, // this is you
    Date creationDate, // you created yourself then
    Date lastLogin, // last addiction time moment
    Date mutedUntil, // you know what you did.
    String name, // yes

    String verificationToken,
    Date verificationTokenTimestamp,
    Date verificationTokenExpiration
) {
}
