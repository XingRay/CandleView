package com.xingray.candleview;

import com.xingray.collection.series.DoubleSeries;
import com.xingray.view.Color;

public interface Line extends DoubleSeries {

    Color getColor();

    String getName();
}
