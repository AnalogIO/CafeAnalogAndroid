package dk.cafeanalog;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AnalogWidget extends AppWidgetProvider {
    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        new AnalogWidgetTask(context).execute();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {
            new AnalogWidgetTask(context).execute();
        }
    }

    private static PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, AnalogWidget.class);
        intent.setAction(SYNC_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private class AnalogWidgetTask extends Communicator.AnalogTask {
        public AnalogWidgetTask(final Context context) {
            super(
                    new Communicator.Runnable<Boolean>() {
                        @Override
                        public void run(final Boolean param) {
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                            builder.setSmallIcon(R.drawable.ic_closed_analog);
                            builder.setContentTitle(context.getText(R.string.app_name));
                            builder.setContentText(context.getText(R.string.refreshing_analog));
                            builder.setOngoing(true);
                            final NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                            manager.notify(MainActivity.notificationId, builder.build());
                            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, AnalogWidget.class));
                            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.analog_widget);
                            CharSequence widgetText = context.getText(R.string.refreshing_analog);
                            Log.i("AnalogWidget", "Fetching");
                            views.setTextViewText(R.id.appwidget_text, widgetText);
                            views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.primary_text_dark));
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
                                        widgetText = context.getString(R.string.widget_open_analog);
                                        views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_green_light));

                                    } else {
                                        widgetText = context.getString(R.string.widget_closed_analog);
                                        views.setTextColor(R.id.appwidget_text, context.getResources().getColor(android.R.color.holo_red_light));
                                    }

                                    builder.setContentText(context.getText(R.string.is_open_analog));
                                    manager.notify(MainActivity.notificationId, builder.build());
                                    views.setTextViewText(R.id.appwidget_text, widgetText);
                                    views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context));
                                    // Instruct the widget manager to update the widget
                                    for (int appWidgetId : appWidgetIds) {
                                        appWidgetManager.updateAppWidget(appWidgetId, views);
                                    }
                                }
                            }, 500);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.analog_widget);
                            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, AnalogWidget.class));
                            views.setTextViewText(R.id.appwidget_text, "Error");
                            views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context));
                            // Instruct the widget manager to update the widget
                            for (int appWidgetId : appWidgetIds) {
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    }
            );
        }
    }
}

