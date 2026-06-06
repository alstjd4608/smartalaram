package com.example.smartalaram_1;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizGenerator {

    private static final Random random = new Random();

    /**
     * Generate a quiz, randomly picking from custom quizzes or math quizzes.
     * If custom quizzes exist, there's a 50% chance of getting one.
     */
    public static Quiz generateQuiz(Context context) {
        CustomQuizStorage storage = new CustomQuizStorage(context);
        List<CustomQuiz> customQuizzes = storage.loadQuizzes();

        // If custom quizzes exist, 50% chance to use one
        if (!customQuizzes.isEmpty() && random.nextBoolean()) {
            return fromCustomQuiz(customQuizzes.get(random.nextInt(customQuizzes.size())));
        }

        return generateMathQuiz();
    }

    /**
     * Generate a math-only quiz (fallback when no context available).
     */
    public static Quiz generateMathQuiz() {
        int type = random.nextInt(3);
        switch (type) {
            case 0: return generateMultiplicationQuiz();
            case 1: return generateAdditionQuiz();
            case 2: return generateSubtractionQuiz();
            default: return generateMultiplicationQuiz();
        }
    }

    private static Quiz fromCustomQuiz(CustomQuiz custom) {
        List<String> options = new ArrayList<>(custom.getOptions());
        // Shuffle options and track correct answer
        String correctAnswer = options.get(custom.getCorrectIndex());
        Collections.shuffle(options);
        int newCorrectIndex = options.indexOf(correctAnswer);

        return new Quiz(custom.getQuestion(), options, newCorrectIndex, true, "커스텀");
    }

    private static Quiz generateMultiplicationQuiz() {
        int a = random.nextInt(12) + 2;
        int b = random.nextInt(12) + 2;
        int answer = a * b;
        String question = a + " × " + b + " = ?";
        return createMathQuiz(question, answer, "곱셈");
    }

    private static Quiz generateAdditionQuiz() {
        int a = random.nextInt(90) + 10;
        int b = random.nextInt(90) + 10;
        int answer = a + b;
        String question = a + " + " + b + " = ?";
        return createMathQuiz(question, answer, "덧셈");
    }

    private static Quiz generateSubtractionQuiz() {
        int a = random.nextInt(90) + 50;
        int b = random.nextInt(40) + 10;
        int answer = a - b;
        String question = a + " - " + b + " = ?";
        return createMathQuiz(question, answer, "뺄셈");
    }

    private static Quiz createMathQuiz(String question, int correctAnswer, String type) {
        List<String> options = new ArrayList<>();
        options.add(String.valueOf(correctAnswer));

        while (options.size() < 4) {
            int offset = random.nextInt(20) - 10;
            if (offset == 0) offset = random.nextInt(10) + 1;
            int wrongAnswer = correctAnswer + offset;
            String wrongStr = String.valueOf(wrongAnswer);
            if (wrongAnswer > 0 && !options.contains(wrongStr)) {
                options.add(wrongStr);
            }
        }

        Collections.shuffle(options);
        int correctIndex = options.indexOf(String.valueOf(correctAnswer));

        return new Quiz(question, options, correctIndex, false, type);
    }

    public static class Quiz {
        private final String question;
        private final List<String> options;
        private final int correctIndex;
        private final boolean isCustom;
        private final String type;

        public Quiz(String question, List<String> options, int correctIndex, boolean isCustom, String type) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
            this.isCustom = isCustom;
            this.type = type;
        }

        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
        public boolean isCustom() { return isCustom; }
        public String getType() { return type; }
        public boolean isCorrect(int selectedIndex) { return selectedIndex == correctIndex; }
    }
}
