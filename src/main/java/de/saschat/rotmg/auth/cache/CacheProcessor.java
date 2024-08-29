package de.saschat.rotmg.auth.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class CacheProcessor {
    public static CacheProcessor CURRENT = null;
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void storeIfPossible(String id, String data) {
        if(CURRENT != null)
            CURRENT.store(id, data);
    }
    public static String retrieveIfPossible(String id) {
        if(CURRENT != null)
            return CURRENT.retrieve(id);
        return null;
    }


    public abstract String retrieve(String id);
    public abstract void store(String id, String data);
}
