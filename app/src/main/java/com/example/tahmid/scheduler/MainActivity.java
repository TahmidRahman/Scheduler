package com.example.tahmid.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.tahmid.scheduler.widgets.TimePickerFragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView textViewAlarmTime;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private Button alarmButton;

    public static final String HOUR_OF_DAY = "hour_of_day";
    public static final String MINUTE = "minute";
    public static final String ALARM_SET = "alarm_set";
    public static final String SCHEDULE = "schedule";

    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;
    private Intent broadcastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewAlarmTime = findViewById(R.id.textViewAlarm);
        sharedPreferences = getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        alarmButton = findViewById(R.id.button2);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        broadcastIntent = new Intent(this,AlarmBroadCastReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(this,0,broadcastIntent,0);

        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(ALARM_SET)) {
                    updateViewOnAlarmSet(sharedPreferences,key);
                } else {
                    updateViewOnTimeSet(sharedPreferences,key);
                }
            }
        };
    }

    private void startRecurringWork() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SchedulerWorker.class,1, TimeUnit.HOURS).build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    protected void onStart()  {
        super.onStart();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        if(sharedPreferences.contains(SCHEDULE)) {
            updateViewOnTimeSet(sharedPreferences,SCHEDULE);
        }

        if(sharedPreferences.getBoolean(ALARM_SET,false)) {
            updateViewOnAlarmSet(sharedPreferences,ALARM_SET);
//            startRecurringWork();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private void updateViewOnTimeSet(SharedPreferences prefs, String key) {
        String textViewText = "Your alarm will be set at " + prefs.getString(key,"00:00");
        textViewAlarmTime.setText(textViewText);
        alarmButton.setEnabled(true);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onHandleSelectedTime(HashMap<String,Integer> time) {
        writeTimeSharedPreference(time);
    }

    public void writeTimeSharedPreference(HashMap<String,Integer> time) {
        String hourOfDay = String.format("%02d",time.get(HOUR_OF_DAY));
        String minute = String.format("%02d",time.get(MINUTE));
        sharedPreferences.edit().putString(SCHEDULE,hourOfDay+":"+minute).apply();
    }

    public void updateViewOnAlarmSet(SharedPreferences prefs, String alarmSetKey) {
        if(prefs.getBoolean(alarmSetKey,false) && prefs.contains(SCHEDULE)) {
            String textViewText = "Your alarm is set at " + prefs.getString(SCHEDULE,"00:00");
            textViewAlarmTime.setText(textViewText);
            alarmButton.setText("Cancel alarm");
            alarmButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_alarm_off_black_24dp),null,null,null);

        } else {
            textViewAlarmTime.setText("Cleared alarm");
            alarmButton.setText("Set alarm");
            alarmButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.alarm_button_drawable),null,null,null);
        }
    }

    public void setAlarmInSystem() {
        String times[] = sharedPreferences.getString(SCHEDULE,"00:00").split(":");
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.HOUR_OF_DAY,Integer.parseInt(times[0]));
        currentTime.set(Calendar.MINUTE,Integer.parseInt(times[1]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,currentTime.getTimeInMillis(),alarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,currentTime.getTimeInMillis(),alarmPendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,currentTime.getTimeInMillis(),alarmPendingIntent);
        }
    }

    public void handleToggleAlarm(View v) {
        if(sharedPreferences.getBoolean(ALARM_SET,false) && sharedPreferences.contains(SCHEDULE)) {
            alarmManager.cancel(alarmPendingIntent);
            sharedPreferences.edit().putBoolean(ALARM_SET,false).apply();
        } else {
            setAlarmInSystem();
            sharedPreferences.edit().putBoolean(ALARM_SET,true).apply();
        }
    }
}
