package my.edu.utar.evercare.Statistics.HeartRate;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class HeartRateData implements Parcelable {
    private String heartRate;
    private Date date;

    public HeartRateData(String heartRate, Date date) {
        this.heartRate = heartRate;
        this.date = date;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public Date getDate() {
        return date;
    }

    // Parcelable implementation
    protected HeartRateData(Parcel in) {
        heartRate = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<HeartRateData> CREATOR = new Creator<HeartRateData>() {
        @Override
        public HeartRateData createFromParcel(Parcel in) {
            return new HeartRateData(in);
        }

        @Override
        public HeartRateData[] newArray(int size) {
            return new HeartRateData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(heartRate);
        dest.writeSerializable(date);
    }
}
