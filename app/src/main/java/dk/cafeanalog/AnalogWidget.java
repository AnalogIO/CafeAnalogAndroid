package dk.cafeanalog;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AnalogWidget extends AppWidgetProvider {
    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.analog_widget);
        new Communicator.AnalogTask(
                new Communicator.Runnable<Boolean>() {
                    @Override
                    public void run(Boolean param) {
                        CharSequence widgetText;
                        if (param) {
                            widgetText = context.getString(R.string.widget_open_analog);
                            views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_green_light));

                        } else {
                            widgetText = context.getString(R.string.widget_closed_analog);
                            views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_red_light));
                        }

                        views.setTextViewText(R.id.appwidget_text, widgetText);
                        // Instruct the widget manager to update the widget
                        for (int appWidgetId : appWidgetIds) {
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        views.setTextViewText(R.id.appwidget_text, "Error");
                        // Instruct the widget manager to update the widget
                        for (int appWidgetId : appWidgetIds) {
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }
                }
        ).execute();

        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context));
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, AnalogWidget.class));
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.analog_widget);

            new Communicator.AnalogTask(
                    new Communicator.Runnable<Boolean>() {
                        @Override
                        public void run(Boolean param) {
                            CharSequence widgetText;
                            if (param) {
                                widgetText = context.getString(R.string.widget_open_analog);
                                views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_green_light));

                            } else {
                                widgetText = context.getString(R.string.widget_closed_analog);
                                views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_red_light));
                            }

                            views.setTextViewText(R.id.appwidget_text, widgetText);
                            // Instruct the widget manager to update the widget
                            for (int appWidgetId : appWidgetIds) {
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            views.setTextViewText(R.id.appwidget_text, "Error");
                            // Instruct the widget manager to update the widget
                            for (int appWidgetId : appWidgetIds) {
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    }
            ).execute();
        }
    }

    private PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(SYNC_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}

