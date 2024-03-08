package my.edu.utar.evercare.Statistics.Weight;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class WeightData implements Parcelable {
    private String weight;
    private Date date;

    public WeightData(String weight, Date date) {
        this.weight = weight;
        this.date = date;
    }

    public String getWeight() {
        return weight;
    }

    public Date getDate() {
        return date;
    }

    // Parcelable implementation
    protected WeightData(Parcel in) {
        weight = in.readString();
        date = (Date) in.readSerializable();
    }

    public static final Creator<WeightData> CREATOR = new Creator<WeightData>() {
        @Override
        public WeightData createFromParcel(Parcel in) {
            return new WeightData(in);
        }

        @Override
        public WeightData[] newArray(int size) {
            return new WeightData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(weight);
        dest.writeSerializable(date);
    }
}
