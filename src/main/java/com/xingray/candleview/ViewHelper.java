package com.xingray.candleview;

import com.xingray.javabase.interfaces.DoubleMapper;
import com.xingray.view.Canvas;

public class ViewHelper {

    public static void drawLine(Canvas canvas, double[] xPositions, Line line, DoubleMapper<Double> mapper) {
        int k = 0;
        int size = line.length();
        double[] yValues = new double[size];
        double[] xValues = new double[size];

        for (int j = 0; j < size; j++) {
            double value = line.get(j);
            if (Double.isNaN(value)) {
                continue;
            }

            yValues[k] = mapper.map(value);
            xValues[k] = xPositions[j];
            k++;
        }
        canvas.getPaint().setDrawColor(line.getColor());
        canvas.drawPolyline(xValues, yValues, k);
    }
}
