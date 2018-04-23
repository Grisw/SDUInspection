package pers.lxt.sduinspection.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.RestRequest;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Urls;
import pers.lxt.sduinspection.util.MD5Utils;

/**
 * Token Restful Service.
 * It's a singleton class, use {@code getInstance} to get the instance.
 */
public class TokenService {

    private static TokenService _tokenService;
    public static TokenService getInstance(Context context){
        if(_tokenService == null){
            _tokenService = new TokenService(context);
        }
        return _tokenService;
    }

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private String phone;
    private String token;

    private TokenService(Context context){
        requestQueue = Volley.newRequestQueue(context);
        sharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    /**
     * Validate the account and request a token from server.
     * @param phone Phone number.
     * @param password Password, which would be digested when sending request.
     * @return Response object contains a token.
     * @throws InterruptedException Throws if the thread is interrupted.
     * @throws JSONException Throws if the parameters generation process went wrong.
     * @throws ServiceException If causes by JSONException: cannot resolve messages received from server.
     *                          If causes by VolleyError: received error response from server, or a network error.
     */
    public Response<String> requestToken(String phone, String password) throws InterruptedException, JSONException, ServiceException {
        JSONObject requestParam = new JSONObject();
        requestParam.put("phoneNumber", phone);
        requestParam.put("password", MD5Utils.getMD5(password));

        final Response<String> response = new Response<>();
        final ServiceException exception = new ServiceException();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Urls.TOKEN,
                requestParam,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                            response.setObject(s.getString("body"));
                        } catch (JSONException e) {
                            exception.initCause(e);
                        }

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        exception.initCause(volleyError);

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                });

        // Add to request queue.
        requestQueue.add(request);

        // Wait response.
        synchronized (response){
            response.wait();
        }

        // Throw ServiceException if error occurred while requesting.
        if(exception.getCause() != null){
            throw exception;
        }

        return response;
    }

    public Response<Void> changePassword(String password, String oldPassword, String pn, String token) throws InterruptedException, JSONException, ServiceException {
        JSONObject requestParam = new JSONObject();
        requestParam.put("password", MD5Utils.getMD5(password));
        requestParam.put("oldPassword", MD5Utils.getMD5(oldPassword));

        final Response<Void> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.POST,
                Urls.TOKEN_PWD,
                requestParam,
                pn, token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                        } catch (JSONException e) {
                            exception.initCause(e);
                        }

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        exception.initCause(volleyError);

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                });

        // Add to request queue.
        requestQueue.add(request);

        // Wait response.
        synchronized (response){
            response.wait();
        }

        // Throw ServiceException if error occurred while requesting.
        if(exception.getCause() != null){
            throw exception;
        }

        return response;
    }

    public Response<Void> deleteToken(String pn, String token) throws InterruptedException, ServiceException {
        final Response<Void> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.DELETE,
                Urls.TOKEN,
                pn,
                token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                        } catch (Exception e) {
                            exception.initCause(e);
                        }

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        exception.initCause(volleyError);

                        // Wake up main Thread.
                        synchronized (response){
                            response.notify();
                        }
                    }
                });

        // Add to request queue.
        requestQueue.add(request);

        // Wait response.
        synchronized (response){
            response.wait();
        }

        // Throw ServiceException if error occurred while requesting.
        if(exception.getCause() != null){
            throw exception;
        }

        return response;
    }

    /**
     * Get registered token string.
     * @return token string cached in local variables,
     *         or read from SharedPreferences if local cache is null.
     */
    public String getToken() {
        if(token == null){
            token = sharedPreferences.getString("token", null);
        }
        return token;
    }

    /**
     * Save token with phone and token string.
     * @param phone phone number.
     * @param token token string.
     */
    public void setToken(String phone, String token) {
        this.phone = phone;
        this.token = token;

        // write SharedPreference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("phone", phone);
        editor.apply();
    }

    /**
     * Get registered phone number.
     * @return phone number cached in local variables,
     *         or read from SharedPreferences if local cache is null.
     */
    public String getPhone() {
        if(phone == null){
            phone = sharedPreferences.getString("phone", null);
        }
        return phone;
    }
}
