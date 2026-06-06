package com.example.smartalaram_1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "smart_alarm_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("alarm_id", -1);
        String label = intent.getStringExtra("alarm_label");
        int hour = intent.getIntExtra("alarm_hour", 0);
        int minute = intent.getIntExtra("alarm_minute", 0);

        // Start the alarm sound service
        Intent serviceIntent = new Intent(context, AlarmSoundService.class);
        serviceIntent.putExtra("alarm_id", alarmId);
        context.startForegroundService(serviceIntent);

        // Create notification that opens QuizActivity
        createNotificationChannel(context);
        showNotification(context, alarmId, label, hour, minute);
    }

    private void createNotificationChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.alarm_channel_description));
        channel.setSound(alarmSound, audioAttributes);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
        channel.setBypassDnd(true);

        manager.createNotificationChannel(channel);
    }

    private void showNotification(Context context, int alarmId, String label, int hour, int minute) {
        Intent quizIntent = new Intent(context, QuizActivity.class);
        quizIntent.putExtra("alarm_id", alarmId);
        quizIntent.putExtra("alarm_hour", hour);
        quizIntent.putExtra("alarm_minute", minute);
        quizIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, alarmId, quizIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String timeStr = String.format("%d:%02d %s", displayHour, minute, hour < 12 ? "AM" : "PM");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setSubText(timeStr)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }
}
