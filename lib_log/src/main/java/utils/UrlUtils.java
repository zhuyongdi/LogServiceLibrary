package utils;

import java.util.Map;

public class UrlUtils {

    public static String getUrlParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder urlSb = new StringBuilder();
        boolean isAppend = false;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && key.trim().length() != 0) {
                if (!isAppend) {
                    urlSb.append("?");
                    isAppend = true;
                } else {
                    urlSb.append("&");
                }
                urlSb.append(key).append("=").append(value);
            }
        }
        return urlSb.toString();
    }

}
