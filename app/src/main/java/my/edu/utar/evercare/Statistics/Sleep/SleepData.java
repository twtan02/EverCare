package my.edu.utar.evercare.Statistics.Sleep;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class SleepData implements Parcelable {
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

    // Parcelable implementation
    protected SleepData(Parcel in) {
        sleepDuration = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<SleepData> CREATOR = new Creator<SleepData>() {
        @Override
        public SleepData createFromParcel(Parcel in) {
            return new SleepData(in);
        }

        @Override
        public SleepData[] newArray(int size) {
            return new SleepData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sleepDuration);
        dest.writeSerializable(date);
    }
}
