package pers.lxt.sduinspection.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import pers.lxt.sduinspection.model.Response;
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

    private String phone;
    private String token;

    private TokenService(Context context){
        requestQueue = Volley.newRequestQueue(context);
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

    /**
     * Check if a token is valid.
     * @param phone Phone number.
     * @param token The token to check.
     * @return Response object contains true if success.
     * @throws InterruptedException Throws if the thread is interrupted.
     * @throws ServiceException If causes by JSONException: cannot resolve messages received from server.
     *                          If causes by VolleyError: received error response from server, or a network error.
     */
    public Response<Boolean> checkToken(String phone, String token) throws InterruptedException, ServiceException {
        final Response<Boolean> response = new Response<>();
        final ServiceException exception = new ServiceException();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Urls.TOKEN + "?phoneNumber="+phone+"&token="+token,
                null,
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
