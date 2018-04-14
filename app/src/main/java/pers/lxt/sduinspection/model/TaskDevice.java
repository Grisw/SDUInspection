package pers.lxt.sduinspection.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;

public class TaskDevice implements Serializable {
    private int taskId;
    private int deviceId;
    private boolean checked;
    private Bitmap picture;
    private Date checkedTime;
    private String name;
    private String description;
    private double latitude;
    private double longitude;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getCheckedTime() {
        return checkedTime;
    }

    public void setCheckedTime(Date checkedTime) {
        this.checkedTime = checkedTime;
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
}
