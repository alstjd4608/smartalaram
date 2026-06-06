package com.example.smartalaram_1;

public class QuizStatItem {
    private String type; // "덧셈", "뺄셈", "곱셈", "커스텀"
    private long timeTakenMs;
    private boolean isCorrect;
    private long timestamp;

    public QuizStatItem(String type, long timeTakenMs, boolean isCorrect, long timestamp) {
        this.type = type;
        this.timeTakenMs = timeTakenMs;
        this.isCorrect = isCorrect;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public long getTimeTakenMs() { return timeTakenMs; }
    public boolean isCorrect() { return isCorrect; }
    public long getTimestamp() { return timestamp; }
}
