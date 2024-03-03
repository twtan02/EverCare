package my.edu.utar.evercare.MedicalRecord;

import android.os.Parcel;
import android.os.Parcelable;

public class Medication implements Parcelable {
    private String id;
    private String medicineName;
    private String dosage;

    public Medication() {
        // Required empty constructor for Firestore
    }

    public Medication(String id, String medicineName, String dosage) {
        this.id= id;
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

    public String getId() {
        return id;
    }

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