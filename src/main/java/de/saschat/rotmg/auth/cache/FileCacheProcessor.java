package de.saschat.rotmg.auth.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FileCacheProcessor extends CacheProcessor {
    CachingSettings settings;
    public FileCacheProcessor(CachingSettings settings) {
        this.settings = settings;
    }
    @Override
    public String retrieve(String id) {
        File read = new File(settings.folder, id);
        if(read.exists()) {
            try(FileInputStream fis = new FileInputStream(read)) {
                return new String(fis.readAllBytes());
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void store(String id, String data) {
        settings.folder.mkdirs();
        try(FileOutputStream stream = new FileOutputStream(id)) {
            stream.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t) {}
    }

    public static record CachingSettings(
        File folder
    ) {
        public static CachingSettings CURRENT = null;
    }
}
