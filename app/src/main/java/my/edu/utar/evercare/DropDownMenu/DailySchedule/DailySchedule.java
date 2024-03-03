package my.edu.utar.evercare.DropDownMenu.DailySchedule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DailySchedule {
    private String day;
    private String event;

    public DailySchedule(String day, String event) {
        this.day = day;
        this.event = event;
    }

    public String getDay() {
        return day;
    }

    public String getEvent() {
        return event;
    }
}
