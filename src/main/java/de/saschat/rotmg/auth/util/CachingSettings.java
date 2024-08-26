package de.saschat.rotmg.auth.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public record CachingSettings(
    File folder
) {
    public static CachingSettings CURRENT = null;
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
