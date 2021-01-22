package com.xingray.candleview;

public interface CandleSeries {

    int length();

    double getOpen(int index);

    double getClose(int index);

    double getHigh(int index);

    double getLow(int index);

    long getVolume(int index);

    long getTimeSecond(int index);
}
