package com.xingray.candleview;


import com.xingray.fxview.FxColor;
import com.xingray.fxview.FxView;
import com.xingray.javabase.range.DoubleRange;
import com.xingray.stock.analysis.candle.Candle;
import com.xingray.stock.analysis.candle.CandleList;
import com.xingray.stock.analysis.indicator.DataList;
import com.xingray.view.Canvas;
import com.xingray.view.Color;
import com.xingray.view.Paint;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CandleView extends FxView {

    // properties
    private Color backgroundLineColor = Color.rgb(50, 50, 50, 50);
    private Color textColor = Color.rgb(100, 50, 50, 50);
    private int textWidth = 40;
    private double barWidthRatio = 0.9;
    private int barCountMin = 0;
    private int barCountMax = 1000;

    private Color upColor = FxColor.toColor(javafx.scene.paint.Color.RED);
    private Color downColor = FxColor.toColor(javafx.scene.paint.Color.GREEN);
    private Color noneColor = FxColor.toColor(javafx.scene.paint.Color.GRAY);

    // data
    private final DataList<Candle> candleList = new CandleList();
    private final List<Line> lines = new ArrayList<>();

    // tmp
    private double heightRatio;
    private DoubleRange valueRange;
    private boolean isDataUpdated;

    private final Logger log = LoggerFactory.getLogger(CandleView.class);
    private double barWidth;
    private List<com.xingray.javabase.interfaces.EventHandler<Candle>> selectEventHandlerList;
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
                DataList<Candle> candleList = CandleView.this.candleList;
                if (candleList == null || candleList.length() == 0) {
                    return;
                }


                int index = (int) (x / barWidth) + firstBarIndex;
                if (index >= candleList.length()) {
                    return;
                }
                if (selectEventHandlerList != null) {
                    for (com.xingray.javabase.interfaces.EventHandler<Candle> handler : selectEventHandlerList) {
                        handler.onEvent(candleList.get(index));
                    }
                }
            }
        });
    }

    public void addSelectEventHandler(com.xingray.javabase.interfaces.EventHandler<Candle> eventHandler) {
        if (selectEventHandlerList == null) {
            selectEventHandlerList = new LinkedList<>();
        }
        selectEventHandlerList.add(eventHandler);
    }

    public void removeSelectEventHandler(com.xingray.javabase.interfaces.EventHandler<Candle> eventHandler) {
        if (selectEventHandlerList == null) {
            return;
        }
        selectEventHandlerList.remove(eventHandler);
    }

    public void setBarWidthRatio(double barWidthRatio) {
        this.barWidthRatio = barWidthRatio;
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

    public void clear() {
        if (candleList.length() == 0 && lines.isEmpty()) {
            return;
        }
        candleList.clear();
        lines.clear();

        isDataUpdated = true;
        invalidate();
    }

    public void setCandleList(List<Candle> candleList) {
        this.candleList.clear();
        if (candleList != null) {
            this.candleList.addAll(candleList);
        }
        isDataUpdated = true;
        invalidate();
    }

    public void addAll(List<Candle> candleList) {
        if (candleList == null) {
            return;
        }
        this.candleList.addAll(candleList);

        isDataUpdated = true;
        invalidate();
    }

    public void add(Candle candle) {
        if (candle == null) {
            return;
        }
        this.candleList.add(candle);

        isDataUpdated = true;
        invalidate();
    }

    private double getY(double v) {
        return 0.95 * getHeight() - (v - valueRange.getStart()) * heightRatio;
    }

    @Override
    public void onDraw(Canvas canvas) {
        DataList<Candle> candleList = this.candleList;
        int length = candleList.length();
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
            valueRange = CandleUtil.getValuesRange(candleList, lines);
        }

        double min = valueRange.getStart();
        double max = valueRange.getEnd();

        if (max != min) {
            heightRatio = (height * 0.9) / (max - min);
        } else {
            heightRatio = height * 0.9;
        }

        barWidth = (width - textWidth) / barCount;
        double halfCandleWidth = (barWidth * barWidthRatio) / 2;

        double[] xPositions = ViewHelper.getPositions(barCount, barWidth);

        drawBackgroundLines(canvas, width, height, min, max, xPositions);
        drawCandles(canvas, candleList, firstBarIndex, barCount, xPositions, halfCandleWidth);
        drawLines(canvas, lines, firstBarIndex, xPositions);
    }

    public void drawCandles(Canvas canvas, DataList<Candle> candleList, int firstBarIndex, int barCount, double[] xPositions, double halfCandleWidth) {
        if (candleList == null || candleList.length() == 0) {
            return;
        }
        int size = Math.min(barCount, candleList.length() - firstBarIndex);
        for (int i = 0; i < size; i++) {
            double position = xPositions[i];
            Candle candle = candleList.get(i + firstBarIndex);
            drawCandle(canvas, position, candle, halfCandleWidth, heightRatio);
        }
    }

    public void drawCandle(Canvas canvas, double position, Candle candle, double halfCandleWidth, double heightRatio) {
        double open = candle.getOpen().doubleValue();
        double close = candle.getClose().doubleValue();
        double high = candle.getHigh().doubleValue();
        double low = candle.getLow().doubleValue();

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
