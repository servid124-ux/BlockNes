package net.BlockNes.players;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class LoginParser {
    private LoginParser() {}

    public static String jwtPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) return null;
        try {
            return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String jsonString(String json, String key) {
        String needle = "\"" + key + "\"";
        int i = 0;
        while ((i = json.indexOf(needle, i)) >= 0) {
            int j = i + needle.length();
            while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
            if (j < json.length() && json.charAt(j) == ':') {
                j++;
                while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                if (j < json.length() && json.charAt(j) == '\"') {
                    StringBuilder sb = new StringBuilder();
                    for (j++; j < json.length(); j++) {
                        char c = json.charAt(j);
                        if (c == '\\' && j + 1 < json.length()) { sb.append(json.charAt(++j)); continue; }
                        if (c == '\"') return sb.toString();
                        sb.append(c);
                    }
                    return null;
                }
            }
            i += needle.length();
        }
        return null;
    }
}
