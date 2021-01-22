package com.xingray.candleview;

import com.xingray.collection.CollectionUtil;
import com.xingray.collection.series.DoubleSeries;
import com.xingray.fxview.FxColor;
import com.xingray.fxview.FxView;
import com.xingray.javabase.range.DoubleRange;
import com.xingray.view.Canvas;
import com.xingray.view.Color;
import com.xingray.view.Paint;

import java.util.ArrayList;
import java.util.List;

public class IndicatorView extends FxView {

    // properties
    private Color backgroundLineColor = Color.rgb(50, 50, 50, 50);
    private Color textColor = Color.rgb(100, 50, 50, 50);
    private int textWidth = 40;

    // data
    private final List<Line> lines;
    private DoubleSeries barSeries;

    // tmp
    private double halfCandleWidth;
    private double heightRatio;
    private boolean isDataUpdated;
    private DoubleRange valueRange;

    public IndicatorView() {
        lines = new ArrayList<>();
    }

    public void clear() {
        this.lines.clear();
        this.barSeries = null;
        invalidate();
    }

    public void setLines(List<Line> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
        isDataUpdated = true;
        invalidate();
    }

    public void addLine(Line line) {
        lines.add(line);
        isDataUpdated = true;
        invalidate();
    }

    public void setBarSeriesList(DoubleSeries barSeries) {
        this.barSeries = barSeries;
        isDataUpdated = true;
        invalidate();
    }

    public void setBackgroundLineColor(Color backgroundLineColor) {
        this.backgroundLineColor = backgroundLineColor;
        invalidate();
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
        invalidate();
    }

    public void notifyDataUpdated() {
        this.isDataUpdated = true;
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isDataUpdated) {
            List<DoubleSeries> series = new ArrayList<>(lines);
            series.add(barSeries);
            valueRange = CollectionUtil.getRangeOfDoubleSeriesList(series);
            isDataUpdated = false;
        }

        int size = 0;
        if (!CollectionUtil.isEmpty(barSeries)) {
            size = barSeries.length();
        } else if (!CollectionUtil.isEmpty(lines)) {
            size = lines.get(0).length();
        }
        if (size == 0) {
            return;
        }

        double width = getWidth();
        double height = getHeight();

        halfCandleWidth = ((width - textWidth) / (2 * (size + 1)));
        double gap = halfCandleWidth * 0.2;
        halfCandleWidth = halfCandleWidth * 0.9;

        double min = valueRange.getStart();
        double max = valueRange.getEnd();

        if (max != min) {
            heightRatio = (height * 0.9) / (max - min);
        } else {
            heightRatio = height * 0.9;
        }

        double[] xPositions = new double[size];
        for (int i = 0; i < size; i++) {
            xPositions[i] = (2 * i + 1) * halfCandleWidth + (i + 1) * gap;
        }

        drawBackgroundLines(canvas, width, height, min, max, xPositions);
        drawBars(canvas, barSeries, xPositions);
        drawLines(canvas, lines, xPositions);
    }

    public void drawBackgroundLines(Canvas canvas, double width, double height, double min, double max, double[] positions) {
        double range = CandleUtil.getSplitRange(min, max, 8);
        double startLineValue = ((int) (min / range)) * range;

        Paint paint = canvas.getPaint();
        paint.setDrawColor(backgroundLineColor);
        paint.setTextColor(textColor);

        for (int i = 0; i < 12; i++) {
            double lineValue = startLineValue + i * range;
            if (lineValue > max) {
                break;
            }
            double lineY = getY(lineValue);
            canvas.drawLine(0, lineY, width, lineY);
            canvas.drawText(Double.toString(lineValue), width - textWidth, lineY);
        }

        for (int i = 0, size = positions.length; i < size; i++) {
            if (i % 5 == 0) {
                double position = positions[i];
                canvas.drawLine(position, 0, position, height);
            }
        }
    }

    private void drawBars(Canvas canvas, DoubleSeries values, double[] xPositions) {
        if (CollectionUtil.isEmpty(values)) {
            return;
        }

        for (int i = 0, size = values.length(); i < size; i++) {
            double value = values.get(i);
            double position = xPositions[i];

            double top = Math.max(value, 0);
            double bottom = Math.min(value, 0);

            Color color;
            if (value > 0) {
                color = FxColor.toColor(javafx.scene.paint.Color.RED);
            } else if (value < 0) {
                color = FxColor.toColor(javafx.scene.paint.Color.GREEN);
            } else {
                color = FxColor.toColor(javafx.scene.paint.Color.GRAY);
            }

            double topY = getY(top);
//            double bottomY = getY(bottom);
            // 柱体
            canvas.getPaint().setFillColor(color);
            canvas.fillRect(position - halfCandleWidth, topY, halfCandleWidth * 2, (top - bottom) * heightRatio);
        }
    }

    public void drawLines(Canvas canvas, List<Line> lines, double[] xPositions) {
//        for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
//            drawLine(canvas, size, xPositions, i);
//        }
        for (Line line : lines) {
            ViewHelper.drawLine(canvas, xPositions, line, 0, this::getY);
        }
    }

//    public void drawLine(Canvas canvas, int size, double[] xPositions, int i) {
//        Line line = lines.get(i);
//        double[] yPositions = new double[size];
//        for (int j = 0; j < size; j++) {
//            yPositions[j] = getY(line.get(j));
//        }
//        canvas.getPaint().setDrawColor(line.getColor());
//        canvas.drawPolyline(xPositions, yPositions, size);
//    }

    private double getY(double v) {
        return 0.95 * getHeight() - (v - valueRange.getStart()) * heightRatio;
    }
}
