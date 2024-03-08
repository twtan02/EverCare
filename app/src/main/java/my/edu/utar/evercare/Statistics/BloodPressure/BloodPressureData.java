package my.edu.utar.evercare.Statistics.BloodPressure;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class BloodPressureData implements Parcelable {
    private String bloodPressureLevel;
    private Date date;

    public BloodPressureData(String bloodPressureLevel, Date date) {
        this.bloodPressureLevel = bloodPressureLevel;
        this.date = date;
    }

    public String getBloodPressureLevel() {
        return bloodPressureLevel;
    }

    public Date getDate() {
        return date;
    }

    // Parcelable implementation
    protected BloodPressureData(Parcel in) {
        bloodPressureLevel = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<BloodPressureData> CREATOR = new Creator<BloodPressureData>() {
        @Override
        public BloodPressureData createFromParcel(Parcel in) {
            return new BloodPressureData(in);
        }

        @Override
        public BloodPressureData[] newArray(int size) {
            return new BloodPressureData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bloodPressureLevel);
        dest.writeSerializable(date);
    }
}
