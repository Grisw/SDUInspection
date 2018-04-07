package pers.lxt.sduinspection.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.RestRequest;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Urls;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.util.MD5Utils;

/**
 * User Restful Service.
 * It's a singleton class, use {@code getInstance} to get the instance.
 */
public class UserService {

    private static UserService _userService;
    public static UserService getInstance(Context context){
        if(_userService == null){
            _userService = new UserService(context);
        }
        return _userService;
    }

    private RequestQueue requestQueue;

    private UserService(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public Response<User> getUser(String phone, String token) throws InterruptedException, ServiceException {
        final Response<User> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.GET,
                Urls.USER + "?phoneNumber="+phone,
                null,
                token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                            if(!s.isNull("body")){
                                JSONObject jsonObject = s.getJSONObject("body");
                                User user = new User();
                                if(jsonObject.isNull("birthday")){
                                    user.setBirthday(null);
                                }else{
                                    user.setBirthday(DateFormat.getDateInstance().parse(jsonObject.getString("birthday")));
                                }
                                user.setEmail(jsonObject.getString("email"));
                                user.setLeader(jsonObject.getString("leader"));
                                user.setName(jsonObject.getString("name"));
                                user.setPhoneNumber(jsonObject.getString("phoneNumber"));
                                user.setSex(User.Sex.valueOf(jsonObject.getString("sex")));
                                response.setObject(user);
                            }
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
}
