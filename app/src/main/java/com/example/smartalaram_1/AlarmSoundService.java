package com.example.smartalaram_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmSoundService extends Service {

    private static final String CHANNEL_ID = "alarm_sound_service_channel";
    private static final int FOREGROUND_ID = 2001;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createServiceChannel();

        Intent quizIntent = new Intent(this, QuizActivity.class);
        quizIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, quizIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("알람이 울리고 있습니다")
                .setContentText("탭하여 알람 끄기")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        int alarmId = intent != null ? intent.getIntExtra("alarm_id", -1) : -1;

        startForeground(FOREGROUND_ID, notification);

        playAlarmSound(alarmId);
        startVibration();

        return START_STICKY;
    }

    private void createServiceChannel() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "알람 서비스",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("알람 소리 재생 서비스");
        manager.createNotificationChannel(channel);
    }

    private void playAlarmSound(int alarmId) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            int volume = 100;
            if (alarmId != -1) {
                AlarmStorage storage = new AlarmStorage(this);
                for (AlarmItem item : storage.loadAlarms()) {
                    if (item.getId() == alarmId) {
                        volume = item.getVolume();
                        break;
                    }
                }
            }
            float volumeFloat = volume / 100f;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );
            mediaPlayer.setVolume(volumeFloat, volumeFloat);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVibration() {
        VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        vibrator = vibratorManager.getDefaultVibrator();

        long[] pattern = {0, 500, 200, 500, 200, 500};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // 사용자가 최근 앱 목록에서 스와이프하여 종료하려고 할 때 서비스 재시작
        Intent restartServiceIntent = new Intent(getApplicationContext(), AlarmSoundService.class);
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            restartServicePendingIntent = PendingIntent.getForegroundService(
                    getApplicationContext(), 1, restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            restartServicePendingIntent = PendingIntent.getService(
                    getApplicationContext(), 1, restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        android.app.AlarmManager alarmService = (android.app.AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    android.os.SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
