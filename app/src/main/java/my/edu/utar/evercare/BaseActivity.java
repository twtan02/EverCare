package my.edu.utar.evercare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String PREF_KEY_FONT_SIZE = "font_size";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyFontSize(getFontSize());
    }

    public void applyFontSize(int fontSize) {
        View rootView = getWindow().getDecorView().getRootView();
        applyFontSizeRecursively(rootView, fontSize);
    }

    private void applyFontSizeRecursively(View view, int fontSize) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                applyFontSizeRecursively(childView, fontSize);
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextSize(fontSize);
        }
    }

    protected int getFontSize() {
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt(PREF_KEY_FONT_SIZE, 18); // Default font size: 18sp
    }

    protected void updateFontSizesForActivities(int fontSize) {
        // This method should be implemented in each activity to update their font size.
    }

    // Rest of your BaseActivity code...
    // ...
}
