package com.example.smartalaram_1;

import java.io.Serializable;
import java.util.Arrays;

public class AlarmItem implements Serializable {
    private int id;
    private int hour;
    private int minute;
    private String label;
    private boolean enabled;
    private boolean[] repeatDays; // 0=Mon, 1=Tue, ..., 6=Sun
    private int volume; // 0 to 100

    public AlarmItem(int id, int hour, int minute, String label, boolean enabled, boolean[] repeatDays, int volume) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.enabled = enabled;
        this.repeatDays = repeatDays != null ? repeatDays : new boolean[7];
        this.volume = volume;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean[] getRepeatDays() { return repeatDays; }
    public void setRepeatDays(boolean[] repeatDays) { this.repeatDays = repeatDays; }
    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public String getFormattedTime() {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format("%d:%02d", displayHour, minute);
    }

    public String getAmPm() {
        return hour < 12 ? "AM" : "PM";
    }

    public boolean hasRepeatDays() {
        for (boolean day : repeatDays) {
            if (day) return true;
        }
        return false;
    }

    public String getRepeatDaysString() {
        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (repeatDays[i]) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(dayNames[i]);
            }
        }
        return sb.toString();
    }
}
