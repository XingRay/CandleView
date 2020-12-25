package com.xingray.candleview;


import com.xingray.view.Color;

import java.util.Arrays;

public class ValueLine implements Line {

    private final double[] values;
    private final Color color;

    public ValueLine(double[] values, Color color) {
        this.values = values;
        this.color = color;
    }

    @Override
    public double get(int index) {
        return values[index];
    }

    @Override
    public int length() {
        return values.length;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Line{" +
                "values=" + Arrays.toString(values) +
                ", color=" + color +
                '}';
    }
}
