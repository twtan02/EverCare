package my.edu.utar.evercare.Statistics.HealthActivity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class HealthActivityData implements Parcelable {
    private String healthActivity;
    private Date date;

    public HealthActivityData(String healthActivity, Date date) {
        this.healthActivity = healthActivity;
        this.date = date;
    }

    public String getHealthActivity() {
        return healthActivity;
    }

    public Date getDate() {
        return date;
    }

    // Parcelable implementation
    protected HealthActivityData(Parcel in) {
        healthActivity = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<HealthActivityData> CREATOR = new Creator<HealthActivityData>() {
        @Override
        public HealthActivityData createFromParcel(Parcel in) {
            return new HealthActivityData(in);
        }

        @Override
        public HealthActivityData[] newArray(int size) {
            return new HealthActivityData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(healthActivity);
        dest.writeSerializable(date);
    }
}
