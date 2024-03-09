package my.edu.utar.evercare.Statistics.BloodLipids;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class BloodLipidsData implements Parcelable {
    private String bloodLipidsLevel;
    private Date date;

    public BloodLipidsData(String bloodLipidsLevel, Date date) {
        this.bloodLipidsLevel = bloodLipidsLevel;
        this.date = date;
    }

    public String getBloodLipidsLevel() {
        return bloodLipidsLevel;
    }

    public Date getDate() {
        return date;
    }

    // Parcelable implementation
    protected BloodLipidsData(Parcel in) {
        bloodLipidsLevel = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<BloodLipidsData> CREATOR = new Creator<BloodLipidsData>() {
        @Override
        public BloodLipidsData createFromParcel(Parcel in) {
            return new BloodLipidsData(in);
        }

        @Override
        public BloodLipidsData[] newArray(int size) {
            return new BloodLipidsData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bloodLipidsLevel);
        dest.writeSerializable(date);
    }
}
