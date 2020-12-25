package com.xingray.candleview;

import com.xingray.collection.CollectionUtil;
import com.xingray.javabase.range.DoubleRange;

import java.util.List;

public class CandleUtil {

    public static double getSplitRange(double min, double max, int n) {
        if (min > max || n < 0) {
            throw new IllegalArgumentException();
        }

        double split = (max - min) / (n * 10);
        return reserveValidNumber(split, 2) * 10;
    }

    public static double reserveValidNumber(double v, int n) {
        //v=242.345 n=2
        //log10=2.xx
        if (v == 0) {
            return 0;
        }
        boolean minus = false;
        if (v < 0) {
            minus = true;
            v = -v;
        }
        double log10 = Math.log10(v);

        //3
        int ceil = (int) Math.ceil(log10);
        //2
        int floor = (int) Math.floor(log10);


        double value = 0;
        for (int i = 0; i < n; i++) {
            double pow = Math.pow(10, floor - i);
            double v1 = ((int) (v / pow)) * pow;
            value += v1;
            v -= v1;
        }

        if (minus) {
            return -value;
        } else {
            return value;
        }
    }

    public static DoubleRange getValuesRange(CandleSeries candleSeries) {
        if (candleSeries == null || candleSeries.length() == 0) {
            return null;
        }

        int length = candleSeries.length();
        double low = candleSeries.getLow(0);
        double high = candleSeries.getHigh(0);

        for (int i = 1; i < length; i++) {
            double lowValue = candleSeries.getLow(i);
            if (lowValue < low) {
                low = lowValue;
            }

            double highValue = candleSeries.getHigh(i);
            if (highValue > high) {
                high = highValue;
            }
        }

        return new DoubleRange(low, high);
    }

    public static DoubleRange getValuesRange(List<Line> lines, DoubleRange range) {
        if (!CollectionUtil.isEmpty(lines)) {
            for (Line line : lines) {
                for (int i = 0, size = line.length(); i < size; i++) {
                    double value = line.get(i);
                    if (value < range.getStart()) {
                        range.setStart(value);
                    }
                    if (value > range.getEnd()) {
                        range.setEnd(value);
                    }
                }
            }
        }
        return range;
    }
}
