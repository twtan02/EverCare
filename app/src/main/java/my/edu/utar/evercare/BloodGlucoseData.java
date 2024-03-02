package my.edu.utar.evercare;

import java.util.Date;

public class BloodGlucoseData {
    private String bloodGlucoseLevel;
    private Date date;

    public BloodGlucoseData(String bloodGlucoseLevel, Date date) {
        this.bloodGlucoseLevel = bloodGlucoseLevel;
        this.date = date;
    }

    public String getBloodGlucoseLevel() {
        return bloodGlucoseLevel;
    }

    public Date getDate() {
        return date;
    }
}
