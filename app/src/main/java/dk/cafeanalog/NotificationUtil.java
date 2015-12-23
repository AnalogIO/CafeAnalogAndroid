package dk.cafeanalog;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

public class NotificationUtil {
    private static final int notificationId = 42584937;
    public static void setNotification(Context context, int stringId, int iconId) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_sticky_notification", false)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(iconId);
            builder.setContentTitle(context.getText(R.string.app_name));
            builder.setContentText(context.getText(stringId));

            Intent intent = new Intent();
            intent.setClass(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

            manager.notify(notificationId, builder.build());
        } else {
            manager.cancelAll();
        }
    }
}
