package com.ecosense.app.fragment;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by MITS on 12/09/2017.
 */

class MyBarChartXAxisValueFormatter implements IAxisValueFormatter {


    private String[] mValues;

    public MyBarChartXAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mValues[(int) value];
    }

}
