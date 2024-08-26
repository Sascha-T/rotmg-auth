module de.saschat.rotmg.auth {
    exports de.saschat.rotmg.auth;
    exports de.saschat.rotmg.auth.util;
    exports de.saschat.rotmg.auth.exceptions;
    exports de.saschat.rotmg.auth.accounts;
    exports de.saschat.rotmg.auth.accounts.deka;
    exports de.saschat.rotmg.auth.accounts.steam;

    requires com.google.gson;
    requires java.xml;
}
