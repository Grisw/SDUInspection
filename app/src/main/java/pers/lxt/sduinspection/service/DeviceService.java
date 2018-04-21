package pers.lxt.sduinspection.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.RestRequest;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Urls;

/**
 * Device Restful Service.
 * It's a singleton class, use {@code getInstance} to get the instance.
 */
public class DeviceService {

    private static DeviceService _deviceService;
    public static DeviceService getInstance(Context context){
        if(_deviceService == null){
            _deviceService = new DeviceService(context);
        }
        return _deviceService;
    }

    private RequestQueue requestQueue;

    private DeviceService(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public Response<List<Device>> getDevices(String pn, String token) throws InterruptedException, ServiceException {
        final Response<List<Device>> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.GET,
                Urls.DEVICE_ALL,
                pn,
                token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                            if(!s.isNull("body")){
                                JSONArray jsonArray = s.getJSONArray("body");
                                List<Device> list = new ArrayList<>();
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Device device = new Device();
                                    device.setDescription(jsonObject.getString("description"));
                                    device.setId(jsonObject.getInt("id"));
                                    device.setLatitude(jsonObject.getDouble("latitude"));
                                    device.setLongitude(jsonObject.getDouble("longitude"));
                                    device.setName(jsonObject.getString("name"));
                                    JSONArray issueArray = jsonObject.getJSONArray("issues");
                                    List<Issue> issues = new ArrayList<>();
                                    for (int k = 0; k < issueArray.length(); k++){
                                        Issue issue = new Issue();
                                        JSONObject issueObject = issueArray.getJSONObject(k);
                                        issue.setTaskId(issueObject.getInt("taskId"));
                                        issue.setId(issueObject.getInt("id"));
                                        issue.setDeviceId(issueObject.getInt("deviceId"));
                                        issue.setTitle(issueObject.getString("title"));
                                        issue.setDescription(issueObject.getString("description"));
                                        issue.setCreator(issueObject.getString("creator"));
                                        issue.setCreatorName(issueObject.getString("creatorName"));
                                        issue.setPicture(issueObject.getString("picture"));
                                        issue.setPublishTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(issueObject.getString("publishTime")));
                                        issue.setState(Issue.State.valueOf(issueObject.getString("state")));
                                        issues.add(issue);
                                    }
                                    device.setIssues(issues);
                                    list.add(device);
                                }
                                response.setObject(list);
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

    public Response<Integer> createDevice(Device device, String pn, String token) throws InterruptedException, ServiceException, JSONException {
        JSONObject body = new JSONObject();
        body.put("name", device.getName());
        body.put("description", device.getDescription());
        body.put("longitude", device.getLongitude());
        body.put("latitude", device.getLatitude());

        final Response<Integer> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.PUT,
                Urls.DEVICE,
                body,
                pn,
                token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                            if(!s.isNull("body")){
                                response.setObject(s.getInt("body"));
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
