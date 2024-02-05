package my.edu.utar.evercare;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterOnlyNumeric implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char character = source.charAt(i);
            if (Character.isDigit(character)) {
                stringBuilder.append(character);
            }
        }
        return stringBuilder.toString();
    }
}
