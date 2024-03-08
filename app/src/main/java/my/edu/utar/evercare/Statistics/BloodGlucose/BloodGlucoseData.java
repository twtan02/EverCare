package my.edu.utar.evercare.Statistics.BloodGlucose;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class BloodGlucoseData implements Parcelable {
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

    // Parcelable implementation
    protected BloodGlucoseData(Parcel in) {
        bloodGlucoseLevel = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<BloodGlucoseData> CREATOR = new Creator<BloodGlucoseData>() {
        @Override
        public BloodGlucoseData createFromParcel(Parcel in) {
            return new BloodGlucoseData(in);
        }

        @Override
        public BloodGlucoseData[] newArray(int size) {
            return new BloodGlucoseData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bloodGlucoseLevel);
        dest.writeSerializable(date);
    }
}
