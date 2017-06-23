package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {
    private static final String LOG_TAG = ChartActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        LineChart chart = (LineChart) findViewById(R.id.chart);

        String symbol = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        String history = getHistory(symbol);
        String[] historyValues = history.split("\\n");

        List<Entry> entries = new ArrayList<Entry>();
        final List<String> dates = new ArrayList<String>();

        for(int i = historyValues.length - 1; i >= 0; i--) {

            String[] valuePair = historyValues[i].split(",");
            Timestamp timestamp = new Timestamp(Long.parseLong(valuePair[0]));

            entries.add(new Entry(historyValues.length - 1 - i,
                    Float.parseFloat(valuePair[1])));
            dates.add(timestamp.toString());
        }

        LineDataSet stockDataSet = new LineDataSet(entries, symbol);
        stockDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int)value);
            }

            /** this is only needed if numbers are returned, else return 0 */
            public int getDecimalDigits() { return 0; }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        LineData data = new LineData(stockDataSet);
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);
        chart.invalidate();
    }

    private String getHistory(String symbol) {
        String history = "init";
        Cursor c = getContentResolver().query(Contract.Quote.URI,
                new String[]{Contract.Quote.COLUMN_HISTORY},
                Contract.Quote.COLUMN_SYMBOL + "=?",
                new String[]{symbol},
                null);

        // Determine the column index of the column named "history"
        int index = c.getColumnIndex(Contract.Quote.COLUMN_HISTORY);

        if (c != null) {
            while (c.moveToNext()) {
                history = c.getString(index);
            }
            return history;

        } else {
            return null;
        }
    }


}
