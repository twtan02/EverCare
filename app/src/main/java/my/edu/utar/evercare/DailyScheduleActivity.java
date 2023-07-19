package my.edu.utar.evercare;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private List<CalendarDate> eventList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_schedule);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calendarView = findViewById(R.id.calendarView);

        // Set up the list of events
        eventList = new ArrayList<>();
        // Add your events to the list
        // eventList.add(new CalendarDate(getDate(year, month, day), "Event 1"));
        // eventList.add(new CalendarDate(getDate(year, month, day), "Event 2"));
        // eventList.add(new CalendarDate(getDate(year, month, day), "Event 3"));
        // Add more events as needed

        // Set the date change listener for the CalendarView
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Date selectedDate = getDate(year, month, dayOfMonth);
                String selectedEvent = getEventForDate(selectedDate);
                if (selectedEvent != null) {
                    Toast.makeText(DailyScheduleActivity.this, selectedEvent, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    private String getEventForDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(date);
        for (CalendarDate calendarDate : eventList) {
            String eventDateString = dateFormat.format(calendarDate.getDate());
            if (dateString.equals(eventDateString)) {
                return calendarDate.getEvent();
            }
        }
        return null;
    }
}
