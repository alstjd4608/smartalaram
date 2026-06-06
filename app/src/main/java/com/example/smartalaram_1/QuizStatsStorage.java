package com.example.smartalaram_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class QuizStatsStorage {

    private final DBHelper dbHelper;

    public QuizStatsStorage(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void addStat(QuizStatItem stat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("type", stat.getType());
        values.put("time_taken_ms", stat.getTimeTakenMs());
        values.put("is_correct", stat.isCorrect() ? 1 : 0);
        values.put("timestamp", stat.getTimestamp());

        db.insert("quiz_stats", null, values);
        db.close();
    }

    public List<QuizStatItem> loadStats() {
        List<QuizStatItem> stats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT type, time_taken_ms, is_correct, timestamp FROM quiz_stats ORDER BY timestamp DESC",
                null
        );

        while (cursor.moveToNext()) {
            QuizStatItem stat = new QuizStatItem(
                    cursor.getString(0),
                    cursor.getLong(1),
                    cursor.getInt(2) == 1,
                    cursor.getLong(3)
            );
            stats.add(stat);
        }

        cursor.close();
        db.close();

        return stats;
    }
}