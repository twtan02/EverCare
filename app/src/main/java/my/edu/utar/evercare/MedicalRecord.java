package my.edu.utar.evercare;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MedicalRecord implements Parcelable {

    private String id;
    private String elderlyId;
    private String elderlyName;
    private String profileImageUrl;
    private List<Medication> medications;

    public MedicalRecord() {
        // Required empty constructor for Firestore
    }

    public MedicalRecord(String elderlyId, String elderlyName, String profileImageUrl, List<Medication> medications) {
        this.elderlyId = elderlyId;
        this.elderlyName = elderlyName;
        this.profileImageUrl = profileImageUrl;
        this.medications = medications;
    }

    public MedicalRecord(ElderlyUser elderlyUser, List<Medication> medications) {
        this.elderlyId = elderlyUser.getUserId();
        this.elderlyName = elderlyUser.getUsername();
        this.profileImageUrl = elderlyUser.getProfileImageUrl();
        this.medications = medications;
    }

    protected MedicalRecord(Parcel in) {
        elderlyId = in.readString();
        elderlyName = in.readString();
        profileImageUrl = in.readString();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getProfileImageUrl() {
        if (profileImageUrl != null) {
            return profileImageUrl;
        } else {
            return "";
        }
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
        dest.writeString(profileImageUrl);
        dest.writeTypedList(medications);
    }
}