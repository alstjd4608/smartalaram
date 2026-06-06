package com.example.smartalaram_1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvResult;
    private TextView btnOption1, btnOption2, btnOption3, btnOption4, btnSubmit;
    private TextView tvQuizAlarmTime;
    private ImageView ivAlarmIcon;
    private TextView[] optionButtons;
    private int selectedOption = -1;
    private QuizGenerator.Quiz currentQuiz;
    private int alarmId;
    
    private int solvedCount = 0;
    private static final int REQUIRED_SOLVED = 5;
    private long quizStartTime;
    private QuizStatsStorage statsStorage;
    private TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            keyguardManager.requestDismissKeyguard(this, null);
        }

        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_quiz);

        statsStorage = new QuizStatsStorage(this);

        alarmId = getIntent().getIntExtra("alarm_id", -1);
        int hour = getIntent().getIntExtra("alarm_hour", 0);
        int minute = getIntent().getIntExtra("alarm_minute", 0);

        initViews();
        setupTime(hour, minute);
        loadNewQuiz();
        startAlarmIconAnimation();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvResult = findViewById(R.id.tvResult);
        tvQuizAlarmTime = findViewById(R.id.tvQuizAlarmTime);
        ivAlarmIcon = findViewById(R.id.ivAlarmIcon);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnSubmit = findViewById(R.id.btnSubmit);
        
        // Progress text
        tvProgress = findViewById(R.id.tvProgress);

        optionButtons = new TextView[]{btnOption1, btnOption2, btnOption3, btnOption4};

        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].setOnClickListener(v -> selectOption(index));
        }

        btnSubmit.setOnClickListener(v -> checkAnswer());
    }

    private void setupTime(int hour, int minute) {
        String currentTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        tvQuizAlarmTime.setText(currentTime);
    }

    private void startAlarmIconAnimation() {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivAlarmIcon, "rotation", -15f, 15f);
        rotateAnimator.setDuration(200);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.start();
    }

    private void loadNewQuiz() {
        currentQuiz = QuizGenerator.generateQuiz(this);
        tvQuestion.setText(currentQuiz.getQuestion());

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(currentQuiz.getOptions().get(i));
            optionButtons[i].setSelected(false);
            optionButtons[i].setBackground(getDrawable(R.drawable.bg_quiz_option));
        }

        selectedOption = -1;
        tvResult.setVisibility(View.GONE);
        
        if (tvProgress != null) {
            tvProgress.setText("진행도: " + solvedCount + " / " + REQUIRED_SOLVED);
        }
        
        quizStartTime = System.currentTimeMillis();
    }

    private void selectOption(int index) {
        selectedOption = index;
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setBackground(getDrawable(R.drawable.bg_quiz_option));
            optionButtons[i].setSelected(i == index);
        }
    }

    private void checkAnswer() {
        if (selectedOption == -1) {
            Toast.makeText(this, "답을 선택해주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        long timeTaken = System.currentTimeMillis() - quizStartTime;
        boolean isCorrect = currentQuiz.isCorrect(selectedOption);

        // 통계 저장
        statsStorage.addStat(new QuizStatItem(currentQuiz.getType(), timeTaken, isCorrect, System.currentTimeMillis()));

        if (isCorrect) {
            solvedCount++;
            optionButtons[selectedOption].setBackground(getDrawable(R.drawable.bg_quiz_correct));
            
            if (solvedCount >= REQUIRED_SOLVED) {
                // All questions solved
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText(getString(R.string.correct_answer) + " 모두 풀었습니다!");
                tvResult.setTextColor(getColor(R.color.quiz_correct));
                
                if (tvProgress != null) {
                    tvProgress.setText("진행도: " + solvedCount + " / " + REQUIRED_SOLVED);
                }

                stopAlarm();

                tvResult.postDelayed(() -> {
                    Toast.makeText(QuizActivity.this, getString(R.string.alarm_off), Toast.LENGTH_LONG).show();
                    finish();
                }, 1500);
            } else {
                // Correct but need more
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText("정답! " + (REQUIRED_SOLVED - solvedCount) + "문제 남았습니다.");
                tvResult.setTextColor(getColor(R.color.quiz_correct));
                
                tvResult.postDelayed(this::loadNewQuiz, 1000);
            }
        } else {
            // Wrong answer
            tvResult.setVisibility(View.VISIBLE);
            tvResult.setText(getString(R.string.wrong_answer));
            tvResult.setTextColor(getColor(R.color.quiz_wrong));

            optionButtons[selectedOption].setBackground(getDrawable(R.drawable.bg_quiz_wrong));

            tvResult.postDelayed(this::loadNewQuiz, 1500);
        }
    }

    private void stopAlarm() {
        Intent serviceIntent = new Intent(this, AlarmSoundService.class);
        stopService(serviceIntent);
        AlarmReceiver.cancelNotification(this);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "5문제를 모두 풀어야 알람이 꺼집니다!", Toast.LENGTH_SHORT).show();
    }
}
