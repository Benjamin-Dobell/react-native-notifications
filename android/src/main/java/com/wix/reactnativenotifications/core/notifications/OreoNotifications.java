package com.wix.reactnativenotifications.core.notifications;

import android.app.Notification;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class OreoNotifications {

    public static void initialize(Context context) {
        Log.d(LOGTAG, "OreoNotifications.initialize()");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createDefaultChannel(context);
    }

    private static String getChannelName(Context context) {
        return getChannelId(context);
    }

    public static String getChannelId(Context context) {
        return context.getPackageName() + "_default";
    }

    public static void setChannel(Context context, Notification.Builder builder) {
        Log.d(LOGTAG, "OreoNotifications.setChannel()");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        builder.setChannelId(getChannelId(context));
    }

    @TargetApi(26)
    private static void createDefaultChannel(Context context) {
        Log.d(LOGTAG, "OreoNotifications.createDefaultChannel()");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = getChannelId(context);
        String channelName = getChannelName(context);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(channelName);

        notificationManager.createNotificationChannel(channel);
    }
}
