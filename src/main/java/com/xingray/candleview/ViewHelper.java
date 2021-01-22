package com.xingray.candleview;

import com.xingray.javabase.interfaces.DoubleMapper;
import com.xingray.view.Canvas;

public class ViewHelper {

    public static void drawLine(Canvas canvas, double[] xPositions, Line line, int firstBarIndex, DoubleMapper<Double> mapper) {
        int k = 0;
        int size = line.length() - firstBarIndex;
        double[] yValues = new double[size];
        double[] xValues = new double[size];

        for (int i = 0; i < size; i++) {
            double value = line.get(i + firstBarIndex);
            if (Double.isNaN(value)) {
                continue;
            }

            yValues[k] = mapper.map(value);
            xValues[k] = xPositions[i];
            k++;
        }
        canvas.getPaint().setDrawColor(line.getColor());
        canvas.drawPolyline(xValues, yValues, k);
    }
}
