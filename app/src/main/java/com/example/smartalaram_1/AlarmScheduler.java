package com.example.smartalaram_1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmScheduler {

    public static void scheduleAlarm(Context context, AlarmItem alarm) {
        if (!alarm.isEnabled()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("alarm_label", alarm.getLabel());
        intent.putExtra("alarm_hour", alarm.getHour());
        intent.putExtra("alarm_minute", alarm.getMinute());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarm.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time is in the past, set it for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                        new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                        pendingIntent
                );
            } else {
                Toast.makeText(context, "정확한 알람 권한이 필요합니다. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(context, "알람 설정 권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }
    }

    public static void cancelAlarm(Context context, AlarmItem alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarm.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
