package my.edu.utar.evercare;

import android.os.Parcel;
import android.os.Parcelable;

public class Medication implements Parcelable {
    private String medicineName;
    private String dosage;

    public Medication() {
        // Required empty constructor for Firestore
    }

    public Medication(String medicineName, String dosage) {
        this.medicineName = medicineName;
        this.dosage = dosage;
    }

    protected Medication(Parcel in) {
        medicineName = in.readString();
        dosage = in.readString();
    }

    public static final Parcelable.Creator<Medication> CREATOR = new Parcelable.Creator<Medication>() {
        @Override
        public Medication createFromParcel(Parcel in) {
            return new Medication(in);
        }

        @Override
        public Medication[] newArray(int size) {
            return new Medication[size];
        }
    };

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(medicineName);
        dest.writeString(dosage);
    }
}