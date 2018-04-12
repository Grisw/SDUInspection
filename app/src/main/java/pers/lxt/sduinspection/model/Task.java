package pers.lxt.sduinspection.model;

import android.content.Context;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import pers.lxt.sduinspection.R;

public class Task implements Serializable {
    public enum State{
        T(R.string.prompt_waiting),
        D(R.string.prompt_executing),
        E(R.string.prompt_finished);

        private int state;
        State(int state){
            this.state = state;
        }

        public String getState(Context context) {
            return context.getString(state);
        }
    }

    private int id;
    private String title;
    private String description;
    private String assignee;
    private State state;
    private Date publishTime;
    private Date dueTime;
    private String creator;
    private String creatorName;
    private String assigneeName;
    private List<TaskDevice> devices;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getDueTime() {
        return dueTime;
    }

    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public List<TaskDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<TaskDevice> devices) {
        this.devices = devices;
    }
}
