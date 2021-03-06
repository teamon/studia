package db;

import java.util.*;

public class TimeEntry extends DbObject {

    protected Date startTime;
    protected Date endTime;
    protected User user;
    protected Task task;

    public TimeEntry(){
        super();
    }

    public TimeEntry(int id){
        this();
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endtime) {
        this.endTime = endtime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public static Collection<TimeEntry> all(){
        return Database.odb.getObjects(TimeEntry.class);
    }

}
