package my.edu.utar.evercare.Statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LineGraphView extends View {

    private List<Float> glucoseLevels;
    private List<Date> dates;
    private Paint paintAxes;
    private Paint paintLine;
    private Paint paintTitle;
    private float xAxisMin, xAxisMax, yAxisMin, yAxisMax;
    private float padding = 150f; // Padding around the graph
    private String yAxisLabel = "Units"; // Default label for y-axis
    private String xAxisTitle = "Date"; // Title for x-axis
    private String graphTitle = ""; // Title for the entire graph
    private int yAxisScaleCount = 5; // Number of scale divisions on the y-axis
    private boolean graphDrawn = false;

    public LineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintAxes = new Paint();
        paintAxes.setColor(Color.BLACK);
        paintAxes.setStrokeWidth(3f); // Thicker axis lines

        paintLine = new Paint();
        paintLine.setColor(Color.BLACK);
        paintLine.setStrokeWidth(5f); // Thicker line

        paintTitle = new Paint();
        paintTitle.setColor(Color.BLACK);
        paintTitle.setTextSize(36f);
        paintTitle.setTextAlign(Paint.Align.CENTER);
    }

    public void setDataPoints(List<Float> glucoseLevels, List<Date> dates) {
        this.glucoseLevels = glucoseLevels;
        this.dates = dates;
        invalidate(); // Refresh view after setting data points
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (glucoseLevels == null || glucoseLevels.isEmpty() || dates == null || dates.isEmpty()) {
            return; // No data points to draw
        }

        int width = getWidth();
        int height = getHeight();

        // Draw graph title
        canvas.drawText(graphTitle, width / 2f, padding * 0.5f, paintTitle);

        // Draw x-axis
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paintAxes);

        // Draw y-axis
        canvas.drawLine(padding, padding, padding, height - padding, paintAxes);

        // Calculate axis ranges
        xAxisMin = padding;
        xAxisMax = width - padding;
        yAxisMin = padding;
        yAxisMax = height - padding;

        // Determine scale factors
        float xRange = xAxisMax - xAxisMin;
        float yRange = yAxisMax - yAxisMin;

        // Plot data points
        float prevX = 0, prevY = 0;
        for (int i = 0; i < glucoseLevels.size(); i++) {
            float x = xAxisMin + i * (xRange / (glucoseLevels.size() - 1));
            float y = yAxisMax - ((glucoseLevels.get(i) - getMinY()) / (getMaxY() - getMinY())) * yRange;

            if (i > 0) {
                // Draw line segment
                canvas.drawLine(prevX, prevY, x, y, paintLine);
            }

            prevX = x;
            prevY = y;
        }

        // Draw x-axis labels
        drawXAxisLabels(canvas, width);

        // Draw y-axis labels and scale
        drawYAxisLabels(canvas, height);

        // Draw x-axis title
        drawXAxisTitle(canvas, width);

        // Draw y-axis title
        drawYAxisTitle(canvas, height);

        // Mark graph as drawn
        graphDrawn = true;
    }

    private float getMinY() {
        return glucoseLevels.stream().min(Float::compareTo).orElse(0f);
    }

    private float getMaxY() {
        return glucoseLevels.stream().max(Float::compareTo).orElse(0f);
    }

    private void drawXAxisLabels(Canvas canvas, int width) {
        if (dates.size() > 0) {
            // Calculate label interval based on the number of dates and available width
            int labelCount = Math.min(5, dates.size());
            float interval = (width - 2 * padding) / (labelCount - 1);

            Paint paintText = new Paint();
            paintText.setColor(Color.BLACK);
            paintText.setTextSize(24f);
            paintText.setTextAlign(Paint.Align.CENTER);

            // Calculate the interval between dates to select evenly distributed dates
            int intervalIndex = (dates.size() - 1) / (labelCount - 1);

            for (int i = 0; i < labelCount; i++) {
                // Calculate the index of the date to be displayed
                int index = i * intervalIndex;
                // Ensure index does not exceed the bounds of the dates list
                index = Math.min(index, dates.size() - 1);

                // Calculate x position for the label
                float x = padding + i * interval;

                // Format date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dateStr = dateFormat.format(dates.get(index));

                // Draw label
                canvas.drawText(dateStr, x, getHeight() - 60, paintText);
            }
        }
    }




    private void drawYAxisLabels(Canvas canvas, int height) {
        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(24f);
        paintText.setTextAlign(Paint.Align.RIGHT);

        float scaleValue = (getMaxY() - getMinY()) / (yAxisScaleCount - 1);

        // Draw labels and scale
        for (int i = 0; i < yAxisScaleCount; i++) {
            float y = padding + (height - 2 * padding) * i / (yAxisScaleCount - 1);
            float value = getMaxY() - i * scaleValue;
            canvas.drawText(String.format("%.1f", value), padding - 30, y, paintText);
        }
    }

    private void drawXAxisTitle(Canvas canvas, int width) {
        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(30f);
        paintText.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paintText.getTextBounds(xAxisTitle, 0, xAxisTitle.length(), bounds);
        int x = width / 2;
        int y = getHeight() - bounds.height() / 2;
        canvas.drawText(xAxisTitle, x, y, paintText);
    }

    private void drawYAxisTitle(Canvas canvas, int height) {
        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(30f);
        paintText.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paintText.getTextBounds(yAxisLabel, 0, yAxisLabel.length(), bounds);
        int x = bounds.height() / 2 + 30; // Adjust this value to move the title closer to the y-axis
        int y = height / 2;
        canvas.save();
        canvas.rotate(-90, x, y);
        canvas.drawText(yAxisLabel, x, y, paintText);
        canvas.restore();
    }
}
