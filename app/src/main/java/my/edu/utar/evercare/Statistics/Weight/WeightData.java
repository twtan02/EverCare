package my.edu.utar.evercare.Statistics.Weight;

import java.util.Date;

public class WeightData {
    private String weightValue;
    private Date date;

    public WeightData(String weightValue, Date date) {
        this.weightValue = weightValue;
        this.date = date;
    }

    public String getWeightValue() {
        return weightValue;
    }

    public Date getDate() {
        return date;
    }
}
