package com.example.smartalaram_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CustomQuizStorage {

    private final DBHelper dbHelper;

    public CustomQuizStorage(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void saveQuizzes(List<CustomQuiz> quizzes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            db.delete("custom_quizzes", null, null);

            for (CustomQuiz quiz : quizzes) {
                List<String> options = quiz.getOptions();

                ContentValues values = new ContentValues();
                values.put("id", quiz.getId());
                values.put("question", quiz.getQuestion());
                values.put("option1", options.get(0));
                values.put("option2", options.get(1));
                values.put("option3", options.get(2));
                values.put("option4", options.get(3));
                values.put("correct_index", quiz.getCorrectIndex());

                db.insert("custom_quizzes", null, values);
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<CustomQuiz> loadQuizzes() {
        List<CustomQuiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, question, option1, option2, option3, option4, correct_index FROM custom_quizzes ORDER BY id DESC",
                null
        );

        while (cursor.moveToNext()) {
            List<String> options = new ArrayList<>();
            options.add(cursor.getString(2));
            options.add(cursor.getString(3));
            options.add(cursor.getString(4));
            options.add(cursor.getString(5));

            CustomQuiz quiz = new CustomQuiz(
                    cursor.getInt(0),
                    cursor.getString(1),
                    options,
                    cursor.getInt(6)
            );

            quizzes.add(quiz);
        }

        cursor.close();
        db.close();

        return quizzes;
    }

    public int getNextId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT MAX(id) FROM custom_quizzes",
                null
        );

        int next = 1;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            next = cursor.getInt(0) + 1;
        }

        cursor.close();
        db.close();

        return next;
    }
}