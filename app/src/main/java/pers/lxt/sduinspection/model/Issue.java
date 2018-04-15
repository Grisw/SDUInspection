package pers.lxt.sduinspection.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Issue implements Serializable {

    public enum State{
        O,
        C
    }

    private int id;
    private int deviceId;
    private Integer taskId;
    private Bitmap picture;
    private String description;
    private String title;
    private String creator;
    private String creatorName;
    private Date publishTime;
    private State state;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getPictureBase64() {
        if(picture == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setPicture(String base64Picture) {
        byte[] data = Base64.decode(base64Picture, Base64.DEFAULT);
        this.picture = BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
