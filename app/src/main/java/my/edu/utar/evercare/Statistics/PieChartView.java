package my.edu.utar.evercare.Statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
    private Paint paint;
    private float[] values;
    private int[][] colors;
    private float strokeWidth;
    private int strokeColor;

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        strokeWidth = 2; // default stroke width
        strokeColor = 0xFF000000; // default stroke color (black)
    }

    public void setData(float[] values, int[][] colors) {
        this.values = values;
        this.colors = colors;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || colors == null || values.length != colors.length) {
            return;
        }

        float total = calculateTotal();

        float startAngle = 0;
        paint.setStrokeWidth(strokeWidth);

        // Calculate the center and radius of the pie chart
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;

        for (int i = 0; i < values.length; i++) {
            paint.setColor(colors[i][0]);
            float sweepAngle = 360 * (values[i] / total);
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }


    private float calculateTotal() {
        float total = 0;
        for (float value : values) {
            total += value;
        }
        return total;
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
    }
}
