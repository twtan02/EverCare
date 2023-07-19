package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FontSizeActivity extends AppCompatActivity {

    private TextView textViewSampleText;
    private SeekBar seekBarFontSize;
    private Button buttonApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);

        textViewSampleText = findViewById(R.id.textViewSampleText);
        seekBarFontSize = findViewById(R.id.seekBarFontSize);
        buttonApply = findViewById(R.id.buttonApply);

        // Set the initial font size
        int initialFontSize = 18; // You can set any initial font size you want
        textViewSampleText.setTextSize(initialFontSize);

        // Set the seek bar progress to the initial font size
        seekBarFontSize.setProgress(initialFontSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the font size of the sample text view based on the seek bar progress
                textViewSampleText.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used in this example
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used in this example
            }
        });

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected font size from the seek bar and apply it to the app's UI
                int selectedFontSize = seekBarFontSize.getProgress();
                // You can save this font size in SharedPreferences or other storage methods to apply it throughout the app
                // For this example, we're just updating the sample text view's font size
                textViewSampleText.setTextSize(selectedFontSize);
            }
        });
    }
}
