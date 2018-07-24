package com.wix.reactnativenotifications.core.notifications.actions;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import com.wix.reactnativenotifications.core.JsIOHelper;
import com.wix.reactnativenotifications.core.LocalNotificationService;

import static com.wix.reactnativenotifications.Defs.ACTION_FIRED_EVENT_NAME;
import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class Action {

    private static final String NAME = "name";
    private static final String REQUEST_CODE = "requestCode";

    private static final String ICON = "icon";
    private static final String TITLE = "title";
    private static final String REMOTE_INPUT = "remoteInput";
    private static final String ALLOW_GENERATED_REPLIES = "allowGeneratedReplies";

    private static final String REMOTE_INPUT_KEY = "key";
    private static final String REMOTE_INPUT_LABEL = "label";
    private static final String REMOTE_INPUT_CHOICES = "choices";

    private static final String FIRED_EVENT_NOTIFICATION_ID = "notificationId";
    private static final String FIRED_EVENT_ACTION = "action";

    private final Bundle properties;

    public Action(final Bundle properties) {
        this.properties = properties;
    }

    public NotificationCompat.Action build(final Context context, final int notificationId) {
        final String name = properties.getString(NAME);
        final Integer iconId = drawableIdFromString(context, properties.getString(ICON));
        final String title = properties.getString(TITLE);

        if (name == null || iconId == null || title == null) {
            Log.e(LOGTAG, NAME + ", " + ICON + " and " + TITLE + " are all required fields for notification actions.");
            return null;
        }

        final PendingIntent pendingIntent = buildPendingIntent(context, notificationId, name);
        final RemoteInput remoteInput = buildRemoteInput();

        final NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(iconId, title, pendingIntent);

        if (remoteInput != null) {
            actionBuilder.addRemoteInput(remoteInput);
        }

        actionBuilder.setAllowGeneratedReplies(properties.getBoolean(ALLOW_GENERATED_REPLIES, false));

        return actionBuilder.build();
    }

    public void onFired(final Context context, final int notificationId) {
        final Bundle eventProperties = new Bundle();
        eventProperties.putInt(FIRED_EVENT_NOTIFICATION_ID, notificationId);
        eventProperties.putBundle(FIRED_EVENT_ACTION, properties);

        final JsIOHelper jsIOHelper = new JsIOHelper(context);
        jsIOHelper.sendEventToJS(ACTION_FIRED_EVENT_NAME, eventProperties);
    }

    private PendingIntent buildPendingIntent(final Context context, final int notificationId, final String name) {
        final String action = "android.intent.action."  + name.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
        final int requestCode = properties.getInt(REQUEST_CODE, notificationId);

        final Intent intent = new Intent(context, LocalNotificationService.class);
        intent.setAction(action);
        intent.putExtra(LocalNotificationService.EXTRA_NOTIFICATION_ID, notificationId);
        intent.putExtra(LocalNotificationService.EXTRA_ACTION, properties);

        return PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private RemoteInput buildRemoteInput() {
        final Bundle remoteInput = properties.getBundle(REMOTE_INPUT);

        if (remoteInput != null) {
            final String key = remoteInput.getString(REMOTE_INPUT_KEY);

            if (key != null) {
                final String label = remoteInput.getString(REMOTE_INPUT_LABEL);
                final String[] choices = remoteInput.getStringArray(REMOTE_INPUT_CHOICES);

                final RemoteInput.Builder remoteInputBuilder = new RemoteInput.Builder(key);
                remoteInputBuilder.setLabel(label);

                if (choices != null && choices.length > 0) {
                    remoteInputBuilder.setChoices(choices);
                    remoteInputBuilder.setAllowFreeFormInput(false);
                }

                return remoteInputBuilder.build();
            } else {
                Log.e(LOGTAG, REMOTE_INPUT_KEY + " is required when specifying " + REMOTE_INPUT + " in a notification action");
            }
        }

        return null;
    }

    @Nullable
    private Integer drawableIdFromString(final Context context, final String string) {
        if (string != null) {
            int id = context.getResources().getIdentifier(string, "drawable", context.getPackageName());

            if (id != 0) {
                return id;
            }
        }

        return null;
    }
}
