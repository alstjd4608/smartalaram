package com.example.smartalaram_1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomQuizActivity extends AppCompatActivity implements CustomQuizAdapter.OnQuizDeleteListener {

    private RecyclerView rvCustomQuizzes;
    private LinearLayout emptyStateQuiz;
    private TextView tvQuizCount;
    private FrameLayout fabAddQuiz;

    private CustomQuizAdapter adapter;
    private List<CustomQuiz> quizList;
    private CustomQuizStorage quizStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custom_quiz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customQuizMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        loadQuizzes();
    }

    private void initViews() {
        rvCustomQuizzes = findViewById(R.id.rvCustomQuizzes);
        emptyStateQuiz = findViewById(R.id.emptyStateQuiz);
        tvQuizCount = findViewById(R.id.tvQuizCount);
        fabAddQuiz = findViewById(R.id.fabAddQuiz);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        fabAddQuiz.setOnClickListener(v -> showAddQuizDialog());

        quizStorage = new CustomQuizStorage(this);
    }

    private void setupRecyclerView() {
        quizList = new ArrayList<>();
        adapter = new CustomQuizAdapter(quizList, this);
        rvCustomQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvCustomQuizzes.setAdapter(adapter);
    }

    private void loadQuizzes() {
        quizList.clear();
        quizList.addAll(quizStorage.loadQuizzes());
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        int count = quizList.size();
        tvQuizCount.setText(count + "개");

        if (count == 0) {
            emptyStateQuiz.setVisibility(View.VISIBLE);
            rvCustomQuizzes.setVisibility(View.GONE);
        } else {
            emptyStateQuiz.setVisibility(View.GONE);
            rvCustomQuizzes.setVisibility(View.VISIBLE);
        }
    }

    private void showAddQuizDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_custom_quiz, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etQuestion = dialogView.findViewById(R.id.etQuizQuestion);
        EditText etOption1 = dialogView.findViewById(R.id.etOption1);
        EditText etOption2 = dialogView.findViewById(R.id.etOption2);
        EditText etOption3 = dialogView.findViewById(R.id.etOption3);
        EditText etOption4 = dialogView.findViewById(R.id.etOption4);
        TextView btnCancelQuiz = dialogView.findViewById(R.id.btnCancelQuiz);
        TextView btnSaveQuiz = dialogView.findViewById(R.id.btnSaveQuiz);

        // Correct answer selection
        final int[] selectedCorrect = {0}; // default: option 1
        TextView[] correctMarkers = {
            dialogView.findViewById(R.id.markerCorrect1),
            dialogView.findViewById(R.id.markerCorrect2),
            dialogView.findViewById(R.id.markerCorrect3),
            dialogView.findViewById(R.id.markerCorrect4)
        };

        // Initialize first as selected
        updateCorrectMarkers(correctMarkers, 0);

        for (int i = 0; i < correctMarkers.length; i++) {
            final int index = i;
            correctMarkers[i].setOnClickListener(v -> {
                selectedCorrect[0] = index;
                updateCorrectMarkers(correctMarkers, index);
            });
        }

        btnCancelQuiz.setOnClickListener(v -> dialog.dismiss());

        btnSaveQuiz.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String opt1 = etOption1.getText().toString().trim();
            String opt2 = etOption2.getText().toString().trim();
            String opt3 = etOption3.getText().toString().trim();
            String opt4 = etOption4.getText().toString().trim();

            // Validation
            if (question.isEmpty()) {
                etQuestion.setError("문제를 입력해주세요");
                return;
            }
            if (opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || opt4.isEmpty()) {
                Toast.makeText(this, "모든 선택지를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = quizStorage.getNextId();
            List<String> options = Arrays.asList(opt1, opt2, opt3, opt4);
            CustomQuiz newQuiz = new CustomQuiz(id, question, options, selectedCorrect[0]);

            quizList.add(0, newQuiz);
            quizStorage.saveQuizzes(quizList);
            adapter.notifyItemInserted(0);
            rvCustomQuizzes.scrollToPosition(0);
            updateUI();

            Toast.makeText(this, "퀴즈가 추가되었습니다!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCorrectMarkers(TextView[] markers, int selectedIndex) {
        for (int i = 0; i < markers.length; i++) {
            if (i == selectedIndex) {
                markers[i].setSelected(true);
                markers[i].setText("✓");
                markers[i].setTextColor(getColor(R.color.quiz_correct));
                markers[i].setBackground(getDrawable(R.drawable.bg_quiz_correct));
            } else {
                markers[i].setSelected(false);
                markers[i].setText(String.valueOf(i + 1));
                markers[i].setTextColor(getColor(R.color.text_hint));
                markers[i].setBackground(getDrawable(R.drawable.bg_day_chip));
            }
        }
    }

    @Override
    public void onQuizDelete(CustomQuiz quiz, int position) {
        new AlertDialog.Builder(this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("퀴즈 삭제")
                .setMessage("이 퀴즈를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    quizList.remove(position);
                    quizStorage.saveQuizzes(quizList);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, quizList.size());
                    updateUI();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
