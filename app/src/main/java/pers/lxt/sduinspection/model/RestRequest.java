package pers.lxt.sduinspection.model;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RestRequest extends JsonObjectRequest {

    private String token;

    public RestRequest(int method, String url, JSONObject jsonRequest, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.token = token;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-INSPECT-TOKEN", token);
        return headers;
    }
}
