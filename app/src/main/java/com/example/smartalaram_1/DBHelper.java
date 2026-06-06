package com.example.smartalaram_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "smart_alarm.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE alarms (" +
                        "id INTEGER PRIMARY KEY, " +
                        "hour INTEGER, " +
                        "minute INTEGER, " +
                        "label TEXT, " +
                        "enabled INTEGER, " +
                        "volume INTEGER, " +
                        "repeat_days TEXT)"
        );

        db.execSQL(
                "CREATE TABLE custom_quizzes (" +
                        "id INTEGER PRIMARY KEY, " +
                        "question TEXT, " +
                        "option1 TEXT, " +
                        "option2 TEXT, " +
                        "option3 TEXT, " +
                        "option4 TEXT, " +
                        "correct_index INTEGER)"
        );

        db.execSQL(
                "CREATE TABLE quiz_stats (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "type TEXT, " +
                        "time_taken_ms INTEGER, " +
                        "is_correct INTEGER, " +
                        "timestamp INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarms");
        db.execSQL("DROP TABLE IF EXISTS custom_quizzes");
        db.execSQL("DROP TABLE IF EXISTS quiz_stats");
        onCreate(db);
    }
}