package my.edu.utar.evercare;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_font, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_font_size) {
            // Show a dialog or any other UI to allow the user to set the font size
            // You can use a SeekBar, NumberPicker, or any other UI component to let the user choose the font size.
            // Once the user selects the font size, you can apply it to your app's text views and save the preference for future usage.

            // Example: Show a simple dialog with a seek bar to select the font size
            showFontSizeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFontSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Font Size");

        // Add a SeekBar to the dialog
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(30); // Adjust the maximum value as needed
        int currentFontSize = 16; // Get the current font size from preferences or set a default value
        seekBar.setProgress(currentFontSize);
        builder.setView(seekBar);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedFontSize = seekBar.getProgress();
                // Apply the selected font size to your app's text views
                // You can use a shared preference to save the font size for future usage

                // For example, to set the font size for a TextView, you can use:
                // textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedFontSize);
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }



}
