package pers.lxt.sduinspection.model;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

public class Urls {

    /**
     * Host address.
     */
    public static final String HOST = "http://10.0.2.2:8080";

    /**
     * Token Service
     */
    public static final String TOKEN = HOST + "/token";

    /**
     * User Service
     */
    public static final String USER = HOST + "/user";

    /**
     * Task Service
     */
    public static final String TASK = HOST + "/task";

    /**
     * Get Task count
     */
    public static final String TASK_COUNT = TASK + "/count";

    /**
     * Make url with GET parameters.
     * @param url Base url.
     * @param params GET params
     * @return Built url, with encoding.
     */
    public static String makeUrl(String url, Map<String, String> params){
        if(params == null || params.size() == 0){
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder(url).append("?");
        Set<Map.Entry<String, String>> entries = params.entrySet();
        for(Map.Entry<String, String> entry : entries){
            try {
                stringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
