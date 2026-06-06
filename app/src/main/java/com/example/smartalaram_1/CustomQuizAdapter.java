package com.example.smartalaram_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomQuizAdapter extends RecyclerView.Adapter<CustomQuizAdapter.QuizViewHolder> {

    private final List<CustomQuiz> quizzes;
    private final OnQuizDeleteListener listener;

    public interface OnQuizDeleteListener {
        void onQuizDelete(CustomQuiz quiz, int position);
    }

    public CustomQuizAdapter(List<CustomQuiz> quizzes, OnQuizDeleteListener listener) {
        this.quizzes = quizzes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        CustomQuiz quiz = quizzes.get(position);

        holder.tvQuizQuestion.setText(quiz.getQuestion());

        // Build options preview text
        StringBuilder optionsText = new StringBuilder();
        List<String> options = quiz.getOptions();
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) optionsText.append("  •  ");
            if (i == quiz.getCorrectIndex()) {
                optionsText.append("✓ ").append(options.get(i));
            } else {
                optionsText.append(options.get(i));
            }
        }
        holder.tvQuizOptions.setText(optionsText.toString());

        holder.tvQuizNumber.setText(String.valueOf(position + 1));

        holder.btnDeleteQuiz.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizDelete(quiz, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizQuestion, tvQuizOptions, tvQuizNumber;
        ImageView btnDeleteQuiz;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizQuestion = itemView.findViewById(R.id.tvQuizQuestion);
            tvQuizOptions = itemView.findViewById(R.id.tvQuizOptions);
            tvQuizNumber = itemView.findViewById(R.id.tvQuizNumber);
            btnDeleteQuiz = itemView.findViewById(R.id.btnDeleteQuiz);
        }
    }
}
