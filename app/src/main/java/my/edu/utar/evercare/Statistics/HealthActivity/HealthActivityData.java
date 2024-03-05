package my.edu.utar.evercare.Statistics.HealthActivity;

import java.util.Date;

public class HealthActivityData {
    private String healthActivityLevel;
    private Date date;

    public HealthActivityData(String healthActivityLevel, Date date) {
        this.healthActivityLevel = healthActivityLevel;
        this.date = date;
    }

    public String getHealthActivityLevel() {
        return healthActivityLevel;
    }

    public Date getDate() {
        return date;
    }
}
