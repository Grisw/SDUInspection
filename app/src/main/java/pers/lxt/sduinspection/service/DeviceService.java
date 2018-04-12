package pers.lxt.sduinspection.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pers.lxt.sduinspection.model.Device;
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
}
