package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class CollectionWidgetRemoteViewsService extends RemoteViewsService {
    private final String LOG_TAG = CollectionWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private  final String LOG_TAG = StockRemoteViewsFactory.class.getSimpleName();
    private Context context;
    private Cursor data = null;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    public StockRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        final Long identityToken = Binder.clearCallingIdentity();
        data = context.getContentResolver().query(Contract.Quote.URI,
                null,
                null,
                null,
                null);
        Binder.restoreCallingIdentity(identityToken);

    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();;
            data = null;
        }

    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.list_item_quote);
        views.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
        views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.change_widget, change);
        } else {
            views.setTextViewText(R.id.change_widget, percentage);
        }

        Log.d(LOG_TAG, data.getString(Contract.Quote.POSITION_SYMBOL));

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if(data.moveToPosition(position))
            return data.getLong(data.getColumnIndex("symbol"));
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
};

