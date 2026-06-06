package com.example.smartalaram_1;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        calculateAndDisplayStats();
    }

    private void calculateAndDisplayStats() {
        QuizStatsStorage storage = new QuizStatsStorage(this);
        List<QuizStatItem> stats = storage.loadStats();

        TextView tvTotalAttempt = findViewById(R.id.tvTotalAttempt);
        TextView tvTotalAccuracy = findViewById(R.id.tvTotalAccuracy);
        TextView tvAvgTime = findViewById(R.id.tvAvgTime);
        TextView tvWeakness = findViewById(R.id.tvWeakness);
        TextView tvStatDetails = findViewById(R.id.tvStatDetails);

        if (stats.isEmpty()) {
            tvTotalAttempt.setText("0문제");
            tvTotalAccuracy.setText("0%");
            tvAvgTime.setText("0초");
            tvWeakness.setText("데이터 부족");
            tvStatDetails.setText("퀴즈를 풀면 통계가 기록됩니다.");
            return;
        }

        int totalAttempts = stats.size();
        int totalCorrect = 0;
        long totalTimeMs = 0;

        // Group by type
        Map<String, TypeStat> typeStats = new HashMap<>();

        for (QuizStatItem stat : stats) {
            if (stat.isCorrect()) totalCorrect++;
            totalTimeMs += stat.getTimeTakenMs();

            String type = stat.getType();
            if (!typeStats.containsKey(type)) {
                typeStats.put(type, new TypeStat());
            }
            
            TypeStat ts = typeStats.get(type);
            ts.attempts++;
            if (stat.isCorrect()) ts.correct++;
            ts.totalTimeMs += stat.getTimeTakenMs();
        }

        // Calculate totals
        int totalAccuracy = (int) (((float) totalCorrect / totalAttempts) * 100);
        float avgTimeSec = (float) (totalTimeMs / totalAttempts) / 1000f;

        tvTotalAttempt.setText(totalAttempts + "문제");
        tvTotalAccuracy.setText(totalAccuracy + "%");
        tvAvgTime.setText(String.format("%.1f초", avgTimeSec));

        // Find weakness (lowest accuracy, or highest time if tie)
        String weaknessType = "없음";
        float lowestAcc = 101f;
        float highestTime = -1f;

        StringBuilder details = new StringBuilder();

        for (Map.Entry<String, TypeStat> entry : typeStats.entrySet()) {
            String type = entry.getKey();
            TypeStat ts = entry.getValue();
            
            float acc = ((float) ts.correct / ts.attempts) * 100;
            float tAvg = (float) (ts.totalTimeMs / ts.attempts) / 1000f;

            details.append(type).append(" : 정답률 ").append((int)acc).append("%")
                   .append(" (평균 ").append(String.format("%.1f", tAvg)).append("초)\n\n");

            if (acc < lowestAcc || (acc == lowestAcc && tAvg > highestTime)) {
                lowestAcc = acc;
                highestTime = tAvg;
                weaknessType = type;
            }
        }

        tvWeakness.setText(weaknessType);
        tvStatDetails.setText(details.toString().trim());
    }

    private static class TypeStat {
        int attempts = 0;
        int correct = 0;
        long totalTimeMs = 0;
    }
}
