package com.xingray.candleview;


import com.xingray.fxview.FxColor;
import com.xingray.fxview.FxView;
import com.xingray.javabase.range.DoubleRange;
import com.xingray.stock.analysis.candle.CandleSeries;
import com.xingray.view.Canvas;
import com.xingray.view.Color;
import com.xingray.view.Paint;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CandleView extends FxView {

    // properties
    private Color backgroundLineColor = Color.rgb(50, 50, 50, 50);
    private Color textColor = Color.rgb(100, 50, 50, 50);
    private int textWidth = 40;
    private double barDrawRatio = 0.9;
    private int barCountMin = 0;
    private int barCountMax = 1000;

    private Color upColor = FxColor.toColor(javafx.scene.paint.Color.RED);
    private Color downColor = FxColor.toColor(javafx.scene.paint.Color.GREEN);
    private Color noneColor = FxColor.toColor(javafx.scene.paint.Color.GRAY);

    // data
    private CandleSeries candleSeries;
    private final List<Line> lines = new ArrayList<>();

    // tmp
    private double halfCandleWidth;
    private double heightRatio;
    private DoubleRange valueRange;
    private boolean isDataUpdated;

    private final Logger log = LoggerFactory.getLogger(CandleView.class);
    private double barWidth;
    private CandleSeriesCallback candleSeriesCallback;
    private int firstBarIndex;

    public CandleView() {
        setOnMouseMoved(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                if (x > (getWidth() - textWidth)) {
                    return;
                }
                double y = event.getY();
                CandleSeries candleSeries = CandleView.this.candleSeries;
                if (candleSeries == null || candleSeries.length() == 0) {
                    return;
                }


                int index = (int) (x / barWidth) + firstBarIndex;
                if (index >= candleSeries.length()) {
                    return;
                }
                if (candleSeriesCallback != null) {
                    candleSeriesCallback.onSelect(candleSeries, index);
                }
            }
        });
    }

    public interface CandleSeriesCallback {
        void onSelect(CandleSeries candleSeries, int index);
    }

    public void setCandleSeriesCallback(CandleSeriesCallback candleSeriesCallback) {
        this.candleSeriesCallback = candleSeriesCallback;
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

    public void setBarCountMin(int barCountMin) {
        this.barCountMin = barCountMin;
    }

    public void setBarCountMax(int barCountMax) {
        this.barCountMax = barCountMax;
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
        CandleSeries candleSeries = this.candleSeries;
        int length = candleSeries == null ? 0 : candleSeries.length();
        if (length == 0) {
            return;
        }
        int barCount = Math.min(barCountMax, Math.max(barCountMin, length));
        if (barCount == 0) {
            return;
        }
        firstBarIndex = Math.max(length - barCount, 0);

        double width = getWidth();
        double height = getHeight();

        if (isDataUpdated) {
            valueRange = CandleUtil.getValuesRange(candleSeries, lines);
        }

        double min = valueRange.getStart();
        double max = valueRange.getEnd();

        if (max != min) {
            heightRatio = (height * 0.9) / (max - min);
        } else {
            heightRatio = height * 0.9;
        }

        barWidth = (width - textWidth) / barCount;
        double halfCandleWidth = (barWidth * barDrawRatio) / 2;

        double[] xPositions = new double[barCount];
        for (int i = 0; i < barCount; i++) {
            xPositions[i] = (i + 0.5) * barWidth;
        }

        drawBackgroundLines(canvas, width, height, min, max, xPositions);
        drawCandles(canvas, candleSeries, firstBarIndex, barCount, xPositions, halfCandleWidth);
        drawLines(canvas, lines, firstBarIndex, xPositions);
    }

    public void drawCandles(Canvas canvas, CandleSeries candleSeries, int firstBarIndex, int barCount, double[] xPositions, double halfCandleWidth) {
        if (candleSeries == null || candleSeries.length() == 0) {
            return;
        }
        int size = Math.min(barCount, candleSeries.length() - firstBarIndex);
        for (int i = 0; i < size; i++) {
            double position = xPositions[i];
            drawCandle(canvas, position, candleSeries, i + firstBarIndex, halfCandleWidth, heightRatio);
        }
    }

    public void drawCandle(Canvas canvas, double position, CandleSeries candleSeries, int index, double halfCandleWidth, double heightRatio) {
        double open = candleSeries.getOpen(index).doubleValue();
        double close = candleSeries.getClose(index).doubleValue();
        double high = candleSeries.getHigh(index).doubleValue();
        double low = candleSeries.getLow(index).doubleValue();

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

    public void drawLines(Canvas canvas, List<Line> lines, int firstBarIndex, double[] xPositions) {
        for (Line line : lines) {
            ViewHelper.drawLine(canvas, xPositions, line, firstBarIndex, this::getY);
        }
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
