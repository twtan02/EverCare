package my.edu.utar.evercare.Statistics.HeartRate;

import java.util.Date;

public class HeartRateData {
    private String heartRateLevel;
    private Date date;

    public HeartRateData(String heartRateLevel, Date date) {
        this.heartRateLevel = heartRateLevel;
        this.date = date;
    }

    public String getHeartRateLevel() {
        return heartRateLevel;
    }

    public Date getDate() {
        return date;
    }
}
