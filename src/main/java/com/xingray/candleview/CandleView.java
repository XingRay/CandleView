package com.xingray.candleview;


import com.xingray.fxview.FxColor;
import com.xingray.fxview.FxView;
import com.xingray.javabase.range.DoubleRange;
import com.xingray.view.Canvas;
import com.xingray.view.Color;
import com.xingray.view.Paint;

import java.util.ArrayList;
import java.util.List;

public class CandleView extends FxView {


    // properties
    private Color backgroundLineColor = Color.rgb(50, 50, 50, 50);
    private Color textColor = Color.rgb(100, 50, 50, 50);
    private int textWidth = 40;

    private Color upColor = FxColor.toColor(javafx.scene.paint.Color.RED);
    private Color downColor = FxColor.toColor(javafx.scene.paint.Color.GREEN);
    private Color noneColor = FxColor.toColor(javafx.scene.paint.Color.GRAY);

    // data
    private CandleSeries candleSeries;
    private List<Line> lines = new ArrayList<>();

    // tmp
    private double halfCandleWidth;
    private double heightRatio;
    private DoubleRange valueRange;
    private boolean isDataUpdated;

    public CandleView() {
    }

    public void setUpColor(Color upColor) {
        this.upColor = upColor;
    }

    public void setDownColor(Color downColor) {
        this.downColor = downColor;
    }

    public void setNoneColor(Color noneColor) {
        this.noneColor = noneColor;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public void setCandleSeries(CandleSeries candleSeries) {
        this.candleSeries = candleSeries;
        isDataUpdated = true;
        invalidate();
    }

//    public void addCandle(Candle candle) {
//        candleSeries.add(candle);
//
//        isDataUpdated = true;
//        invalidate();
//    }
//
//    public void addOrUpdateCandle(Candle candle) {
//        if (candleSeries.isEmpty()) {
//            candleSeries.add(candle);
//            isDataUpdated = true;
//            invalidate();
//            return;
//        }
//
//        int index = candleSeries.size() - 1;
//        Candle lastCandle = candleSeries.get(index);
//        if (lastCandle.getTimeSecond() == candle.getTimeSecond()) {
//            candleSeries.set(index, candle);
//            isDataUpdated = true;
//            invalidate();
//        } else {
//            candleSeries.add(candle);
//            isDataUpdated = true;
//            invalidate();
//        }
//    }

    public void setBackgroundLineColor(Color color) {
        this.backgroundLineColor = color;
        invalidate();
    }

    public void setTextColor(Color color) {
        this.textColor = color;
        invalidate();
    }

    public void addLine(Line line) {
        lines.add(line);
        isDataUpdated = true;
        invalidate();
    }

    public void setLines(List<Line> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
        isDataUpdated = true;
        invalidate();
    }

    public void notifyDataUpdated() {
        isDataUpdated = true;
        invalidate();
    }

    private double getY(double v) {
        return 0.95 * getHeight() - (v - valueRange.getStart()) * heightRatio;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (candleSeries == null || candleSeries.length() == 0) {
            return;
        }

        double width = getWidth();
        double height = getHeight();

        if (isDataUpdated) {
            valueRange = getValuesRange(candleSeries, lines);
        }

        int size = candleSeries.length();

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
        drawCandles(canvas, size, xPositions);
        drawLines(canvas, lines, xPositions);
    }

    public void drawCandles(Canvas canvas, int size, double[] xPositions) {
        if (candleSeries == null || candleSeries.length() == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            double position = xPositions[i];
            drawCandle(canvas, position, candleSeries, i);
        }
    }

    public void drawCandle(Canvas canvas, double position, CandleSeries candleSeries, int index) {
        double open = candleSeries.getOpen(index);
        double close = candleSeries.getClose(index);
        double high = candleSeries.getHigh(index);
        double low = candleSeries.getLow(index);

        double top = Math.max(open, close);
        double bottom = Math.min(open, close);

        Color color;
        if (close > open) {
            color = upColor;
        } else if (close < open) {
            color = downColor;
        } else {
            color = noneColor;
        }

        double topY = getY(top);
        double bottomY = getY(bottom);
        double highY = getY(high);
        double lowY = getY(low);

        if (high > top) {
            // 上影线
            canvas.getPaint().setDrawColor(color);
            canvas.drawLine(position, highY, position, topY);
        }

        // 柱体
        if (close > open) {
            canvas.getPaint().setDrawColor(color);
            canvas.drawRect(position - halfCandleWidth, topY, halfCandleWidth * 2, (top - bottom) * heightRatio);
        } else if (close < open) {
            canvas.getPaint().setFillColor(color);
            canvas.fillRect(position - halfCandleWidth, topY, halfCandleWidth * 2, (top - bottom) * heightRatio);
        } else {
            canvas.getPaint().setDrawColor(color);
            canvas.drawLine(position - halfCandleWidth, topY, position + halfCandleWidth, topY);
        }

        if (low < bottom) {
            // 下影线
            canvas.getPaint().setDrawColor(color);
            canvas.drawLine(position, lowY, position, bottomY);
        }
    }

    public void drawLines(Canvas canvas, List<Line> lines, double[] xPositions) {
        for (Line line : lines) {
            ViewHelper.drawLine(canvas, xPositions, line, this::getY);
        }
    }

    public DoubleRange getValuesRange(CandleSeries candleSeries, List<Line> lines) {
        DoubleRange range = CandleUtil.getValuesRange(candleSeries);
        return CandleUtil.getValuesRange(lines, range);
    }

    public void drawBackgroundLines(Canvas canvas, double width, double height, double min, double max, double[] positions) {
        double range = CandleUtil.getSplitRange(min, max, 10);
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
}
