package com.xingray.candleview;

import com.xingray.stock.analysis.candle.Candle;
import com.xingray.stock.analysis.indicator.Indicator;
import com.xingray.stock.analysis.num.Num;
import com.xingray.view.Color;

public class IndicatorLine implements Line {

    private String name;
    private Indicator<Num, Candle> indicator;
    private Color color;

    public IndicatorLine(Indicator<Num, Candle> indicator, Color color) {
        this("", indicator, color);
    }

    public IndicatorLine(String name, Indicator<Num, Candle> indicator, Color color) {
        this.name = name;
        this.indicator = indicator;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double get(int index) {
        return indicator.get(index).doubleValue();
    }

    @Override
    public int length() {
        return indicator.length();
    }

    @Override
    public Color getColor() {
        return color;
    }
}
