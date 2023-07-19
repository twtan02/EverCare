package my.edu.utar.evercare;

public class CalendarItem {
    private int dayOfMonth;
    private String event;

    public CalendarItem(int dayOfMonth, String event) {
        this.dayOfMonth = dayOfMonth;
        this.event = event;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public String getEvent() {
        return event;
    }
}
