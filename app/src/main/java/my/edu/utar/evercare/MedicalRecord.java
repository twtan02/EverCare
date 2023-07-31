package my.edu.utar.evercare;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MedicalRecord implements Parcelable {
    private String elderlyId;
    private String elderlyName;
    private String profilePicUrl;
    private List<Medication> medications;

    public MedicalRecord() {
        // Required empty constructor for Firestore
    }

    public MedicalRecord(String elderlyId, String elderlyName, String profilePicUrl, List<Medication> medications) {
        this.elderlyId = elderlyId;
        this.elderlyName = elderlyName;
        this.profilePicUrl = profilePicUrl;
        this.medications = medications;
    }

    protected MedicalRecord(Parcel in) {
        elderlyId = in.readString();
        elderlyName = in.readString();
        profilePicUrl = in.readString();
        medications = in.createTypedArrayList(Medication.CREATOR);
    }

    public static final Creator<MedicalRecord> CREATOR = new Creator<MedicalRecord>() {
        @Override
        public MedicalRecord createFromParcel(Parcel in) {
            return new MedicalRecord(in);
        }

        @Override
        public MedicalRecord[] newArray(int size) {
            return new MedicalRecord[size];
        }
    };

    public String getElderlyId() {
        return elderlyId;
    }

    public void setElderlyId(String elderlyId) {
        this.elderlyId = elderlyId;
    }

    public String getElderlyName() {
        return elderlyName;
    }

    public void setElderlyName(String elderlyName) {
        this.elderlyName = elderlyName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(elderlyId);
        dest.writeString(elderlyName);
        dest.writeString(profilePicUrl);
        dest.writeTypedList(medications);
    }
}
