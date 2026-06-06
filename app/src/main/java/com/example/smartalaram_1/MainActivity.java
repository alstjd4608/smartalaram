package com.example.smartalaram_1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmInteractionListener {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvGreeting, tvCurrentTime, tvCurrentDate;
    private TextView tvNextAlarmTime, tvNextAlarmLabel;
    private TextView tvAlarmCount;
    private LinearLayout emptyState;
    private RecyclerView rvAlarms;
    private FrameLayout fabAddAlarm;

    private AlarmAdapter adapter;
    private List<AlarmItem> alarmList;
    private AlarmStorage alarmStorage;
    private Handler timeHandler;
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestPermissions();
        initViews();
        setupRecyclerView();
        loadAlarms();
        startClockUpdater();
        animateFab();
    }

    private void requestPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvNextAlarmTime = findViewById(R.id.tvNextAlarmTime);
        tvNextAlarmLabel = findViewById(R.id.tvNextAlarmLabel);
        tvAlarmCount = findViewById(R.id.tvAlarmCount);
        emptyState = findViewById(R.id.emptyState);
        rvAlarms = findViewById(R.id.rvAlarms);
        fabAddAlarm = findViewById(R.id.fabAddAlarm);

        fabAddAlarm.setOnClickListener(v -> showAddAlarmDialog(null, -1));

        FrameLayout btnQuizManage = findViewById(R.id.btnQuizManage);
        btnQuizManage.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomQuizActivity.class);
            startActivity(intent);
        });

        FrameLayout btnStats = findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
        });

        alarmStorage = new AlarmStorage(this);
    }

    private void setupRecyclerView() {
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList, this);
        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        rvAlarms.setAdapter(adapter);
    }

    private void loadAlarms() {
        alarmList.clear();
        alarmList.addAll(alarmStorage.loadAlarms());
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        int count = alarmList.size();
        tvAlarmCount.setText(count + "개");

        if (count == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvAlarms.setVisibility(View.GONE);
            tvNextAlarmTime.setText(getString(R.string.no_alarm_set));
            tvNextAlarmLabel.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvAlarms.setVisibility(View.VISIBLE);
            updateNextAlarm();
        }
    }

    private void updateNextAlarm() {
        AlarmItem nextAlarm = null;
        long minDiff = Long.MAX_VALUE;
        long now = System.currentTimeMillis();

        for (AlarmItem alarm : alarmList) {
            if (!alarm.isEnabled()) continue;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            cal.set(Calendar.MINUTE, alarm.getMinute());
            cal.set(Calendar.SECOND, 0);

            if (cal.getTimeInMillis() <= now) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            long diff = cal.getTimeInMillis() - now;
            if (diff < minDiff) {
                minDiff = diff;
                nextAlarm = alarm;
            }
        }

        if (nextAlarm != null) {
            tvNextAlarmTime.setText(nextAlarm.getFormattedTime() + " " + nextAlarm.getAmPm());
            if (!nextAlarm.getLabel().isEmpty()) {
                tvNextAlarmLabel.setText(nextAlarm.getLabel());
                tvNextAlarmLabel.setVisibility(View.VISIBLE);
            } else {
                tvNextAlarmLabel.setVisibility(View.GONE);
            }
        } else {
            tvNextAlarmTime.setText(getString(R.string.no_alarm_set));
            tvNextAlarmLabel.setVisibility(View.GONE);
        }
    }

    private void startClockUpdater() {
        timeHandler = new Handler(Looper.getMainLooper());
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateClock();
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void updateClock() {
        Date now = new Date();

        tvCurrentTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        tvCurrentDate.setText(new SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(now));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            tvGreeting.setText(getString(R.string.good_morning));
        } else if (hour >= 12 && hour < 17) {
            tvGreeting.setText(getString(R.string.greeting_afternoon));
        } else if (hour >= 17 && hour < 21) {
            tvGreeting.setText(getString(R.string.greeting_evening));
        } else {
            tvGreeting.setText(getString(R.string.greeting_night));
        }
    }

    private void animateFab() {
        fabAddAlarm.setScaleX(0f);
        fabAddAlarm.setScaleY(0f);
        fabAddAlarm.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    private void showAddAlarmDialog(AlarmItem editAlarm, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_alarm, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvSelectedHour); // This is not title, we need to modify title manually if needed. Actually we didn't add id to title. Let's just find it by type or ignore.
        
        TextView tvSelectedHour = dialogView.findViewById(R.id.tvSelectedHour);
        TextView tvSelectedMinute = dialogView.findViewById(R.id.tvSelectedMinute);
        LinearLayout timePickerLayout = dialogView.findViewById(R.id.timePickerLayout);
        EditText etLabel = dialogView.findViewById(R.id.etAlarmLabel);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnSave = dialogView.findViewById(R.id.btnSave);
        TextView btnDelete = dialogView.findViewById(R.id.btnDelete);
        android.widget.SeekBar sbVolume = dialogView.findViewById(R.id.sbVolume);

        // Day chips
        final boolean[] selectedDays = new boolean[7];
        int[] chipIds = {
            R.id.chipMon, R.id.chipTue, R.id.chipWed,
            R.id.chipThu, R.id.chipFri, R.id.chipSat, R.id.chipSun
        };

        TextView[] chips = new TextView[7];
        for (int i = 0; i < chipIds.length; i++) {
            final int dayIndex = i;
            chips[i] = dialogView.findViewById(chipIds[i]);
            chips[i].setOnClickListener(v -> {
                selectedDays[dayIndex] = !selectedDays[dayIndex];
                chips[dayIndex].setSelected(selectedDays[dayIndex]);
                if (selectedDays[dayIndex]) {
                    chips[dayIndex].setTextColor(getColor(R.color.text_on_primary));
                } else {
                    chips[dayIndex].setTextColor(getColor(R.color.text_secondary));
                }
            });
        }

        final int[] selectedTime = new int[2];
        int currentVolume = 100;

        if (editAlarm != null) {
            // Edit Mode
            btnDelete.setVisibility(View.VISIBLE);
            selectedTime[0] = editAlarm.getHour();
            selectedTime[1] = editAlarm.getMinute();
            etLabel.setText(editAlarm.getLabel());
            currentVolume = editAlarm.getVolume();
            sbVolume.setProgress(currentVolume);

            boolean[] existingDays = editAlarm.getRepeatDays();
            for (int i = 0; i < 7; i++) {
                if (existingDays[i]) {
                    chips[i].performClick();
                }
            }
        } else {
            // New Mode
            Calendar defaultTime = Calendar.getInstance();
            defaultTime.add(Calendar.HOUR_OF_DAY, 1);
            selectedTime[0] = defaultTime.get(Calendar.HOUR_OF_DAY);
            selectedTime[1] = defaultTime.get(Calendar.MINUTE);
        }

        tvSelectedHour.setText(String.format(Locale.getDefault(), "%02d", selectedTime[0]));
        tvSelectedMinute.setText(String.format(Locale.getDefault(), "%02d", selectedTime[1]));

        timePickerLayout.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(selectedTime[0])
                    .setMinute(selectedTime[1])
                    .setTitleText("알람 시간 선택")
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                selectedTime[0] = timePicker.getHour();
                selectedTime[1] = timePicker.getMinute();
                tvSelectedHour.setText(String.format(Locale.getDefault(), "%02d", selectedTime[0]));
                tvSelectedMinute.setText(String.format(Locale.getDefault(), "%02d", selectedTime[1]));
            });

            timePicker.show(getSupportFragmentManager(), "timePicker");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("알람 삭제")
                .setMessage("이 알람을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    AlarmScheduler.cancelAlarm(this, editAlarm);
                    alarmList.remove(position);
                    alarmStorage.saveAlarms(alarmList);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    dialog.dismiss();
                })
                .setNegativeButton("취소", null)
                .show();
        });

        btnSave.setOnClickListener(v -> {
            String label = etLabel.getText().toString().trim();
            int volume = sbVolume.getProgress();

            if (editAlarm != null) {
                // Update existing
                AlarmScheduler.cancelAlarm(this, editAlarm); // Cancel old schedule
                editAlarm.setHour(selectedTime[0]);
                editAlarm.setMinute(selectedTime[1]);
                editAlarm.setLabel(label);
                editAlarm.setRepeatDays(selectedDays);
                editAlarm.setVolume(volume);
                editAlarm.setEnabled(true);
                
                adapter.notifyItemChanged(position);
                AlarmScheduler.scheduleAlarm(this, editAlarm); // Reschedule
            } else {
                // Create new
                int id = alarmStorage.getNextId();
                AlarmItem newAlarm = new AlarmItem(
                        id, selectedTime[0], selectedTime[1],
                        label, true, selectedDays, volume
                );

                alarmList.add(0, newAlarm);
                adapter.notifyItemInserted(0);
                rvAlarms.scrollToPosition(0);
                AlarmScheduler.scheduleAlarm(this, newAlarm);
            }
            
            alarmStorage.saveAlarms(alarmList);
            updateUI();

            int displayHour = selectedTime[0] % 12;
            if (displayHour == 0) displayHour = 12;
            String amPm = selectedTime[0] < 12 ? "AM" : "PM";
            String timeStr = String.format("%d:%02d %s", displayHour, selectedTime[1], amPm);
            Toast.makeText(this, String.format(getString(R.string.alarm_set_for), timeStr), Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onAlarmToggled(AlarmItem alarm, boolean enabled) {
        alarm.setEnabled(enabled);
        alarmStorage.saveAlarms(alarmList);

        if (enabled) {
            AlarmScheduler.scheduleAlarm(this, alarm);
        } else {
            AlarmScheduler.cancelAlarm(this, alarm);
        }

        updateUI();
    }

    @Override
    public void onAlarmClicked(AlarmItem alarm, int position) {
        showAddAlarmDialog(alarm, position);
    }

    @Override
    public void onAlarmLongPressed(AlarmItem alarm, int position) {
        // Option to delete on long press, or can be ignored since delete is in edit dialog.
        // We will keep it as a quick delete shortcut.
        new AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("알람 삭제")
                .setMessage("이 알람을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    AlarmScheduler.cancelAlarm(this, alarm);
                    alarmList.remove(position);
                    alarmStorage.saveAlarms(alarmList);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
}