package com.example.smartalaram_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class AlarmStorage {

    private final DBHelper dbHelper;

    public AlarmStorage(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int getNextId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT MAX(id) FROM alarms",
                null
        );

        int nextId = 1;

        if (cursor.moveToFirst()) {
            nextId = cursor.getInt(0) + 1;
        }

        cursor.close();
        db.close();

        return nextId;
    }

    public void saveAlarms(List<AlarmItem> alarms) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete("alarms", null, null);

        for (AlarmItem alarm : alarms) {
            ContentValues values = new ContentValues();

            values.put("id", alarm.getId());
            values.put("hour", alarm.getHour());
            values.put("minute", alarm.getMinute());
            values.put("label", alarm.getLabel());
            values.put("enabled", alarm.isEnabled() ? 1 : 0);
            values.put("volume", alarm.getVolume());
            values.put("repeat_days", repeatDaysToString(alarm.getRepeatDays()));

            db.insert("alarms", null, values);
        }

        db.close();
    }

    public List<AlarmItem> loadAlarms() {
        List<AlarmItem> alarms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, hour, minute, label, enabled, volume, repeat_days FROM alarms ORDER BY id DESC",
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            int hour = cursor.getInt(1);
            int minute = cursor.getInt(2);
            String label = cursor.getString(3);
            boolean enabled = cursor.getInt(4) == 1;
            int volume = cursor.getInt(5);
            boolean[] repeatDays = stringToRepeatDays(cursor.getString(6));

            AlarmItem alarm = new AlarmItem(
                    id,
                    hour,
                    minute,
                    label,
                    enabled,
                    repeatDays,
                    volume
            );

            alarms.add(alarm);
        }

        cursor.close();
        db.close();

        return alarms;
    }

    private String repeatDaysToString(boolean[] days) {
        if (days == null) {
            return "0000000";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            if (i < days.length && days[i]) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        return sb.toString();
    }

    private boolean[] stringToRepeatDays(String text) {
        boolean[] days = new boolean[7];

        if (text == null) {
            return days;
        }

        for (int i = 0; i < Math.min(text.length(), 7); i++) {
            days[i] = text.charAt(i) == '1';
        }

        return days;
    }
}