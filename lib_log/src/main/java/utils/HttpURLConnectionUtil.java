package utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class HttpURLConnectionUtil {

    public enum SendType {
        FORM_DATA,
        JSON,
        NULL
    }

    /**
     * 发送GET请求
     * @param url 请求的url
     * @param params 请求参数
     */
    public static void sendGet(String url, Map<String, String> params, Callback callback) {
        StringBuilder result = new StringBuilder();
        url += UrlUtils.getUrlParams(params);
        System.out.println("请求的url=" + url);
        Log.i("qqqqqqqqq", "请求的url=" + url);
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL fullUrl = new URL(url);
            connection = (HttpURLConnection) fullUrl.openConnection();

            connection.setRequestMethod("GET");
            /* 设置连接超时时间 */
            connection.setConnectTimeout(60 * 1000);
            /* 设置读取超时时间 */
            connection.setReadTimeout(60 * 1000);
            /* 设置是否在缓存可用的时候使用缓存，post请求不能使用缓存，默认true */
            connection.setUseCaches(false);

            /* 指定客户端可以接受的数据类型 */
            connection.setRequestProperty("accept", "*/*");
            /* 告诉服务器保持长连接 */
            connection.setRequestProperty("connection", "Keep-Alive");
            /* 告诉服务器客户端的一些数据，比如浏览器什么版本，操作系统等等 */
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            /* 告诉服务器客户端请求体的内容类型 */
            connection.setRequestProperty("Content-type", "application/json");

            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            if (callback != null) {
                callback.onSuccess(result.toString());
            }
            Log.i("qqqqqqqqq", "finish,result=" + result.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("qqqqqqqqq", "exception=" + e.getMessage());
            if (callback != null) {
                callback.onFail(e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 发送POST请求
     * @param url 请求的url
     * @param params 请求参数，格式：
     *               {
     *                  username: "123456",
     *                  password: "123456",
     *               }
     */
    public static void sendPost_json(String url, String params, Map<String, String> headerMap, Callback callback) {
        sendPost(SendType.JSON, url, params, headerMap, callback);
    }

    /**
     * 发送POST请求
     *
     * @param url 请求的url
     * @param params 请求参数
     */
    public static void sendPost_formData(String url, Map<String, String> params, Map<String, String> headerMap, Callback callback) {
        sendPost(SendType.FORM_DATA, url, params, headerMap, callback);
    }

    /**
     * 发送POST请求
     * @param sendType Content-Type客户端请求参数类型
     *                 FORM：for
     *                 JSON：json
     * @param url      请求的url
     * @param params   请求参数
     * @param callback 回调函数
     */
    public static void sendPost(SendType sendType, String url, Object params, Map<String, String> headerMap, Callback callback) {
        if (sendType == null) {
            if (callback != null) {
                callback.onFail("invalid params");
            }
            return;
        }
        if (url == null ||
                url.trim().length() == 0 ||
                !(url.startsWith("http://") || url.startsWith("https://"))) {
            if (callback != null) {
                callback.onFail("invalid params");
            }
            return;
        }
        StringBuilder resultSb = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL classURL = new URL(url);
            connection = (HttpURLConnection) classURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "IntelliJ IDEA/2020.2.3");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate,br");
            connection.setRequestProperty("Accept", "*/*");
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            String boundary = null;
            if (sendType == SendType.FORM_DATA) {
                boundary = generateBoundary();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            } else if (sendType == SendType.JSON) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            } else if (sendType == SendType.NULL) {
                connection.setRequestProperty("Content-Type", null);
            }
            byte[] writeBytes = null;
            if (sendType == SendType.JSON) {
                String json = (String) params;
                writeBytes = json.getBytes();
            } else if (sendType == SendType.FORM_DATA) {
                String formData = formData((Map<String, String>) params, boundary);
                System.out.println("formData:" + formData);
                writeBytes = formData.getBytes();
            }
            if (writeBytes != null) {
                connection.setRequestProperty("Content-Length", writeBytes.length + "");
                connection.connect();
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(writeBytes);
                outputStream.flush();
                outputStream.close();
            } else {
                connection.connect();
            }
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                resultSb.append(line);
            }
            if (callback != null) {
                callback.onSuccess(resultSb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFail(e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static InputStream sendPostReturnInputStream(SendType sendType, String url, Object params) {
        if (sendType == null) {
            return null;
        }
        if (url == null ||
                url.trim().length() == 0 ||
                !(url.startsWith("http://") || url.startsWith("https://"))) {
            return null;
        }
        StringBuilder resultSb = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL classURL = new URL(url);
            connection = (HttpURLConnection) classURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "IntelliJ IDEA/2020.2.3");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate,br");
            connection.setRequestProperty("Accept", "*/*");
            String boundary = null;
            if (sendType == SendType.FORM_DATA) {
                boundary = generateBoundary();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            } else if (sendType == SendType.JSON) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            } else if (sendType == SendType.NULL) {
                connection.setRequestProperty("Content-Type", null);
            }
            byte[] writeBytes = null;
            if (sendType == SendType.JSON) {
                String json = (String) params;
                writeBytes = json.getBytes();
            } else if (sendType == SendType.FORM_DATA) {
                String formData = formData((Map<String, String>) params, boundary);
                System.out.println("formData:" + formData);
                writeBytes = formData.getBytes();
            }
            if (writeBytes != null) {
                connection.setRequestProperty("Content-Length", writeBytes.length + "");
                connection.connect();
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(writeBytes);
                outputStream.flush();
                outputStream.close();
            } else {
                connection.connect();
            }
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * ----------------------------a4ccdda9fb8046618ce42a63e7f978f8
     * Content-Disposition: form-data; name="ip"
     *
     * 112.112.11.11
     * ----------------------------a4ccdda9fb8046618ce42a63e7f978f8
     * Content-Disposition: form-data; name="key"
     *
     * c71abfe217768898585750c10c423aca
     * ----------------------------a4ccdda9fb8046618ce42a63e7f978f8--
     */
    private static String formData(Map<String, String> map, String boundary) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append("--")
                    .append(boundary)
                    .append("\r\n")
                    .append("Content-Disposition: form-data; name=\"")
                    .append(key)
                    .append("\"\r\n\r\n")
                    .append(value)
                    .append("\r\n");
        }
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    private static String generateBoundary() {
        return "--------------------------" + UUID.randomUUID().toString().replace("-", "");
    }

    public interface Callback {
        void onSuccess(String result);

        void onFail(String msg);
    }

}
