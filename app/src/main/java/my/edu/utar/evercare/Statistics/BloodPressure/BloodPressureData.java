package my.edu.utar.evercare.Statistics.BloodPressure;

import java.util.Date;

public class BloodPressureData {
    private String bloodPressureLevel;
    private Date date;

    public BloodPressureData(String bloodGlucoseLevel, Date date) {
        this.bloodPressureLevel = bloodGlucoseLevel;
        this.date = date;
    }

    public String getBloodPressureLevel() {
        return bloodPressureLevel;
    }

    public Date getDate() {
        return date;
    }
}
