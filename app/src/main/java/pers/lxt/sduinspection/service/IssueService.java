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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.RestRequest;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.model.Urls;

/**
 * Task Restful Service.
 * It's a singleton class, use {@code getInstance} to get the instance.
 */
public class IssueService {

    private static IssueService _issueService;
    public static IssueService getInstance(Context context){
        if(_issueService == null){
            _issueService = new IssueService(context);
        }
        return _issueService;
    }

    private RequestQueue requestQueue;

    private IssueService(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public Response<Integer> createIssue(Issue issue, String pn, String token) throws InterruptedException, ServiceException, JSONException {
        JSONObject body = new JSONObject();
        body.put("title", issue.getTitle());
        body.put("description", issue.getDescription());
        body.put("deviceId", issue.getDeviceId());
        body.put("taskId", issue.getTaskId());
        body.put("picture", issue.getPictureBase64());

        final Response<Integer> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.PUT,
                Urls.ISSUE,
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
