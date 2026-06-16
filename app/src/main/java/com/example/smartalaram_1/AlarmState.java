package com.example.smartalaram_1;

import android.content.Context;
import android.content.SharedPreferences;

public class AlarmState {

    private static final String PREF_NAME = "alarm_state";
    private static final String KEY_RINGING = "is_ringing";
    private static final String KEY_ALARM_ID = "alarm_id";

    public static void setRinging(Context context, boolean isRinging, int alarmId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putBoolean(KEY_RINGING, isRinging)
                .putInt(KEY_ALARM_ID, alarmId)
                .apply();
    }

    public static boolean isRinging(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_RINGING, false);
    }

    public static int getAlarmId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ALARM_ID, -1);
    }

    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}