package com.xingray.candleview;


import com.xingray.view.Color;

import java.util.Arrays;

public class ValueLine implements Line {

    private double[] values;
    private Color color;
    private String name;

    public ValueLine(double[] values, Color color, String name) {
        this.values = values;
        this.color = color;
        this.name = name;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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
        return "ValueLine{" +
                "values=" + Arrays.toString(values) +
                ", color=" + color +
                ", name='" + name + '\'' +
                '}';
    }
}
