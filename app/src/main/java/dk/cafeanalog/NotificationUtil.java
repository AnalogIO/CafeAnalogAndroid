package dk.cafeanalog;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

/**
 * Created by mikael on 12-11-15.
 */
public class NotificationUtil {
    static final int notificationId = 42584937;
    public static void setNotification(Context context, int stringId, int iconId) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_sticky_notification", true)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(iconId);
            builder.setContentTitle(context.getText(R.string.app_name));
            builder.setContentText(context.getText(stringId));
            builder.setOngoing(true);

            manager.notify(notificationId, builder.build());
        } else {
            manager.cancelAll();
        }
    }
}
