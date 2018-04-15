package pers.lxt.sduinspection.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
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
import pers.lxt.sduinspection.model.User;

/**
 * Task Restful Service.
 * It's a singleton class, use {@code getInstance} to get the instance.
 */
public class TaskService {

    private static TaskService _taskService;
    public static TaskService getInstance(Context context){
        if(_taskService == null){
            _taskService = new TaskService(context);
        }
        return _taskService;
    }

    private RequestQueue requestQueue;

    private TaskService(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public Response<List<Task>> getTasks(String assignee, Task.State state, String pn, String token) throws InterruptedException, ServiceException {
        Map<String, String> getParams = new HashMap<>();
        getParams.put("assignee", assignee);
        getParams.put("state", state.toString());

        final Response<List<Task>> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.GET,
                Urls.makeUrl(Urls.TASK, getParams),
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
                                List<Task> tasks = new ArrayList<>();
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Task task = new Task();
                                    task.setAssignee(jsonObject.getString("assignee"));
                                    task.setCreator(jsonObject.getString("creator"));
                                    task.setDescription(jsonObject.getString("description"));
                                    if(jsonObject.isNull("dueTime")){
                                        task.setDueTime(null);
                                    }else{
                                        task.setDueTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(jsonObject.getString("dueTime")));
                                    }
                                    task.setId(jsonObject.getInt("id"));
                                    task.setPublishTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(jsonObject.getString("publishTime")));
                                    task.setState(Task.State.valueOf(jsonObject.getString("state")));
                                    task.setTitle(jsonObject.getString("title"));
                                    task.setCreatorName(jsonObject.getString("creatorName"));
                                    task.setAssigneeName(jsonObject.getString("assigneeName"));

                                    JSONArray devicesArray = jsonObject.getJSONArray("devices");
                                    List<TaskDevice> taskDevices = new ArrayList<>();
                                    for (int j = 0; j < devicesArray.length(); j++){
                                        TaskDevice device = new TaskDevice();
                                        JSONObject deviceObject = devicesArray.getJSONObject(j);
                                        device.setChecked(deviceObject.getBoolean("checked"));
                                        device.setDescription(deviceObject.getString("description"));
                                        device.setDeviceId(deviceObject.getInt("deviceId"));
                                        device.setTaskId(deviceObject.getInt("taskId"));
                                        device.setLatitude(deviceObject.getDouble("latitude"));
                                        device.setLongitude(deviceObject.getDouble("longitude"));
                                        device.setName(deviceObject.getString("name"));
                                        if(!deviceObject.isNull("picture"))
                                            device.setPicture(deviceObject.getString("picture"));
                                        if(!deviceObject.isNull("checkedTime"))
                                            device.setCheckedTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(deviceObject.getString("checkedTime")));
                                        JSONArray issueArray = deviceObject.getJSONArray("issues");
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
                                            issues.add(issue);
                                        }
                                        device.setIssues(issues);
                                        taskDevices.add(device);
                                    }
                                    task.setDevices(taskDevices);
                                    tasks.add(task);
                                }
                                response.setObject(tasks);
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

    public Response<Map<Task.State, Integer>> getTasksCount(String assignee, String pn, String token) throws InterruptedException, ServiceException {
        Map<String, String> getParams = new HashMap<>();
        getParams.put("assignee", assignee);

        final Response<Map<Task.State, Integer>> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.GET,
                Urls.makeUrl(Urls.TASK_COUNT, getParams),
                pn,
                token,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        try {
                            response.setCode(s.getInt("code"));
                            response.setMessage(s.getString("message"));
                            if(!s.isNull("body")){
                                JSONObject jsonObject = s.getJSONObject("body");
                                Iterator<String> keys = jsonObject.keys();
                                Map<Task.State, Integer> counts = new HashMap<>();
                                while(keys.hasNext()){
                                    String key = keys.next();
                                    counts.put(Task.State.valueOf(key), jsonObject.getInt(key));
                                }
                                response.setObject(counts);
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

    public Response<Void> updateTaskState(int id, Task.State state, String pn, String token) throws InterruptedException, ServiceException, JSONException {
        JSONObject body = new JSONObject();
        body.put("id", id);
        body.put("state", state.toString());

        final Response<Void> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.POST,
                Urls.TASK_STATE,
                body,
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

    public Response<Integer> createTask(String title, String description, String assignee, Date dueTime, List<Integer> devices, String pn, String token) throws InterruptedException, ServiceException, JSONException {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description);
        body.put("assignee", assignee);
        if(dueTime == null){
            body.put("dueTime", null);
        }else{
            body.put("dueTime", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(dueTime));
        }
        body.put("devices", new JSONArray(devices));

        final Response<Integer> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.PUT,
                Urls.TASK,
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

    public Response<Date> updateTaskDevice(TaskDevice device, String pn, String token) throws ServiceException, InterruptedException, JSONException {
        JSONObject body = new JSONObject();
        body.put("taskId", device.getTaskId());
        body.put("deviceId", device.getDeviceId());
        body.put("checked", device.isChecked());
        body.put("picture", device.getPictureBase64());

        final Response<Date> response = new Response<>();
        final ServiceException exception = new ServiceException();
        RestRequest request = new RestRequest(
                Request.Method.POST,
                Urls.TASK_DEVICE,
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
                                response.setObject(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(s.getString("body")));
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
