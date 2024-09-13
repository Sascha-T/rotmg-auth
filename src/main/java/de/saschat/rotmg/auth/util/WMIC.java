package de.saschat.rotmg.auth.util;

import java.io.IOException;

public class WMIC {
    public static String getValue(String path, String value) {
        try {
            Process exec = Runtime.getRuntime().exec(String.format("wmic %s get %s", path, value));
            String text = new String(exec.getInputStream().readAllBytes());
            return text.split(System.lineSeparator())[1].stripIndent().replace("\n", "").replace("\r", "");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
