package com.example.smartalaram_1;

import java.io.Serializable;
import java.util.List;

public class CustomQuiz implements Serializable {
    private int id;
    private String question;
    private List<String> options; // 4 options
    private int correctIndex;     // 0~3

    public CustomQuiz(int id, String question, List<String> options, int correctIndex) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public String getCorrectAnswer() {
        return options.get(correctIndex);
    }
}
