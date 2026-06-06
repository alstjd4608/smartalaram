package com.example.smartalaram_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private final List<AlarmItem> alarms;
    private final OnAlarmInteractionListener listener;

    public interface OnAlarmInteractionListener {
        void onAlarmToggled(AlarmItem alarm, boolean enabled);
        void onAlarmLongPressed(AlarmItem alarm, int position);
        void onAlarmClicked(AlarmItem alarm, int position);
    }

    public AlarmAdapter(List<AlarmItem> alarms, OnAlarmInteractionListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem alarm = alarms.get(position);

        holder.tvAlarmTime.setText(alarm.getFormattedTime());
        holder.tvAlarmAmPm.setText(alarm.getAmPm());
        holder.tvAlarmLabel.setText(alarm.getLabel().isEmpty() ? "알람" : alarm.getLabel());

        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isEnabled());

        // Visual feedback for disabled alarms
        float alpha = alarm.isEnabled() ? 1.0f : 0.5f;
        holder.tvAlarmTime.setAlpha(alpha);
        holder.tvAlarmAmPm.setAlpha(alpha);
        holder.tvAlarmLabel.setAlpha(alpha);

        // Repeat days
        if (alarm.hasRepeatDays()) {
            holder.repeatDaysLayout.setVisibility(View.VISIBLE);
            holder.tvRepeatDays.setText(alarm.getRepeatDaysString());
        } else {
            holder.repeatDaysLayout.setVisibility(View.GONE);
        }

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onAlarmToggled(alarm, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlarmClicked(alarm, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onAlarmLongPressed(alarm, holder.getAdapterPosition());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlarmTime, tvAlarmAmPm, tvAlarmLabel, tvRepeatDays;
        MaterialSwitch switchAlarm;
        LinearLayout repeatDaysLayout;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlarmTime = itemView.findViewById(R.id.tvAlarmTime);
            tvAlarmAmPm = itemView.findViewById(R.id.tvAlarmAmPm);
            tvAlarmLabel = itemView.findViewById(R.id.tvAlarmLabel);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
            repeatDaysLayout = itemView.findViewById(R.id.repeatDaysLayout);
            tvRepeatDays = itemView.findViewById(R.id.tvRepeatDays);
        }
    }
}
