package my.edu.utar.evercare.Statistics.Sleep;

import java.util.Date;

public class SleepData {
    private String sleepDuration;
    private Date date;

    public SleepData(String sleepDuration, Date date) {
        this.sleepDuration = sleepDuration;
        this.date = date;
    }

    public String getSleepDuration() {
        return sleepDuration;
    }

    public Date getDate() {
        return date;
    }
}
