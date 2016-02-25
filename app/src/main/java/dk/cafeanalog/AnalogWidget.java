/*
 * Copyright 2016 Analog IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cafeanalog;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 */
public class AnalogWidget extends AppWidgetProvider {
    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            new AnalogWidgetTask(context).execute();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {
                new AnalogWidgetTask(context).execute();
            } else {
                Toast.makeText(context, "No connection available for refresh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, AnalogWidget.class);
        intent.setAction(SYNC_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private class AnalogWidgetTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        public AnalogWidgetTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            switch (new AnalogDownloader().isOpen()) {
                case OPEN:
                    return true;
                case UNKNOWN:
                    cancel(true);
                case CLOSED:
                default:
                    return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean param) {
            super.onPostExecute(param);
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, AnalogWidget.class));
            final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.analog_widget);
            views.setTextViewText(R.id.appwidget_text, mContext.getText(R.string.refreshing_analog));
            views.setTextColor(R.id.appwidget_text, ContextCompat.getColor(mContext, android.R.color.primary_text_dark));
            // Instruct the widget manager to update the widget
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CharSequence widgetText;
                    if (param) {
                        widgetText = mContext.getString(R.string.widget_open_analog);
                        views.setTextColor(R.id.appwidget_text, ContextCompat.getColor(mContext, android.R.color.holo_green_light));
                    } else {
                        widgetText = mContext.getString(R.string.widget_closed_analog);
                        views.setTextColor(R.id.appwidget_text, ContextCompat.getColor(mContext, android.R.color.holo_red_light));
                    }

                    views.setTextViewText(R.id.appwidget_text, widgetText);
                    views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(mContext));
                    // Instruct the widget manager to update the widget
                    for (int appWidgetId : appWidgetIds) {
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                }
            }, 500);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.analog_widget);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, AnalogWidget.class));
            views.setTextViewText(R.id.appwidget_text, "Error");
            views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(mContext));
            // Instruct the widget manager to update the widget
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}

