package my.edu.utar.evercare.DropDownMenu.FontSize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import my.edu.utar.evercare.BaseActivity;
import my.edu.utar.evercare.Chat.ChatActivity;
import my.edu.utar.evercare.HomepageActivity;
import my.edu.utar.evercare.EmergencyHelp.MapsActivity;
import my.edu.utar.evercare.MedicalRecord.MedicalRecordActivity;
import my.edu.utar.evercare.PillReminder.PillReminderActivity;
import my.edu.utar.evercare.R;
import my.edu.utar.evercare.RemoteMonitoring.RemoteMonitoringActivity;
import my.edu.utar.evercare.Statistics.StatisticsActivity;

public class FontSizeActivity extends BaseActivity {

    private static final String PREF_KEY_FONT_SIZE = "font_size";
    private SharedPreferences sharedPreferences;
    private TextView sampleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int selectedFontSize = sharedPreferences.getInt(PREF_KEY_FONT_SIZE, 18); // Default font size: 18sp

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Change Font");

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RadioGroup fontSizeRadioGroup = findViewById(R.id.fontSizeRadioGroup);
        fontSizeRadioGroup.check(getFontSizeRadioButtonId(selectedFontSize));
        fontSizeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedFontSize = getSelectedFontSize(fontSizeRadioGroup);
                applyFontSize(selectedFontSize);
            }
        });

        Button applyButton = findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedFontSize = getSelectedFontSize(fontSizeRadioGroup);
                applyFontSize(selectedFontSize);

                // Save the selected font size to shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_KEY_FONT_SIZE, selectedFontSize);
                editor.apply();

                // Update font sizes for all relevant activities
                updateFontSizesForActivities(selectedFontSize);

                // Apply the latest font size from shared preferences
                int latestFontSize = sharedPreferences.getInt(PREF_KEY_FONT_SIZE, 18); // Default font size: 18sp
                sampleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, latestFontSize);
            }
        });

        // Sample TextView for preview
        sampleTextView = findViewById(R.id.sampleTextView);
        sampleTextView.setText("EVERCARE"); // You can set the sample text here
        sampleTextView.setTextSize(selectedFontSize);
    }

    private int getFontSizeRadioButtonId(int fontSize) {
        switch (fontSize) {
            case 18:
                return R.id.radioButtonSmall;
            case 22:
                return R.id.radioButtonMedium;
            case 26:
                return R.id.radioButtonLarge;
            default:
                return R.id.radioButtonMedium;
        }
    }

    private int getSelectedFontSize(RadioGroup fontSizeRadioGroup) {
        int checkedId = fontSizeRadioGroup.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radioButtonSmall:
                return 18;
            case R.id.radioButtonMedium:
                return 22;
            case R.id.radioButtonLarge:
                return 26;
            default:
                return 22; // Default font size: 22sp
        }
    }

    // Handle back button click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateFontSizesForActivities(int fontSize) {
        // Get the list of relevant activities
        Class[] activitiesToUpdate = new Class[]{
                HomepageActivity.class,
                PillReminderActivity.class,
                MedicalRecordActivity.class,
                MapsActivity.class,
                ChatActivity.class,
                RemoteMonitoringActivity.class,
                StatisticsActivity.class
        };

        // Update the font size for each activity by sending an intent with the updated font size
        for (Class activityClass : activitiesToUpdate) {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("FONT_SIZE", fontSize);
        }
    }

}
