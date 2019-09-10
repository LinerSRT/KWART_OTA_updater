package com.ota.updates;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Preferences;

public class Settings extends Activity implements Constants {
    private Spinner frequencySpinner;
    private String[] frequencyArray;
    private String[] frequencyArrayValues;

    private CheckBox notificationCheckBox, vibrationCheckBox;
    private Button notificationSoundBtn;

    private boolean showNotification = true;
    private boolean vibrate = false;
    private String ringtonePreference;
    private int selectedFrequency;


    ArrayAdapter<?> qrequencyAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frequencyArrayValues = getResources().getStringArray(R.array.updater_background_frequency_values);
        initValues();
        initViews(AMOLED_VERSION);
        frequencySpinner.setAdapter(qrequencyAdapter);
        updateView();





        Log.d(TAG+"SOUND", "Show notification: "+showNotification+" | Vibrate: "+vibrate+" | Notification sound: "+ringtonePreference);
        notificationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preferences.setBackgroundService(Settings.this, notificationCheckBox.isChecked());
                initValues();
                updateView();

                Log.d(TAG+"SOUND", "Show notification: "+showNotification+" | Vibrate: "+vibrate+" | Notification sound: "+ringtonePreference);
            }
        });

        vibrationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preferences.setNotificationVibrate(Settings.this, vibrationCheckBox.isChecked());
                initValues();
                updateView();

                Log.d(TAG+"SOUND", "Show notification: "+showNotification+" | Vibrate: "+vibrate+" | Notification sound: "+ringtonePreference);
            }
        });

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG+"SPINNER", "selected: "+i+" Value: "+frequencyArrayValues[i]);
                Preferences.setBackgroundFrequency(Settings.this, frequencyArrayValues[i]);
                initValues();
                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        notificationSoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Звук уведомления");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(Preferences.getRingtone(Settings.this)));
                startActivityForResult(intent, 5);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null){
                Preferences.setRingtone(Settings.this, uri.toString());
            }
        }
    }

    private void initValues(){
        showNotification = Preferences.getBackgroundService(this);
        vibrate = Preferences.getNotificationVibrate(this);
        ringtonePreference = Preferences.getRingtone(this);
        selectedFrequency = getSpinnerSelected(Preferences.getBackgroundFrequency(this));
        Log.d(TAG+"SEL", "GET - "+selectedFrequency);


    }

    private int getSpinnerSelected(int value){
        int result = 0;
        if(value == Integer.valueOf(frequencyArrayValues[0])){
            result = 0;
        } else if (value == Integer.valueOf(frequencyArrayValues[1])){
            result = 1;
        } else if (value == Integer.valueOf(frequencyArrayValues[2])){
            result = 2;
        } else if (value == Integer.valueOf(frequencyArrayValues[3])){
            result = 3;
        } else if (value == Integer.valueOf(frequencyArrayValues[4])){
            result = 4;
        } else if (value == Integer.valueOf(frequencyArrayValues[5])){
            result = 5;
        } else if (value == Integer.valueOf(frequencyArrayValues[6])){
            result = 6;
        } else if (value == Integer.valueOf(frequencyArrayValues[7])){
            result = 7;
        } else if (value == Integer.valueOf(frequencyArrayValues[8])){
            result = 8;
        }

        return result;
    }

    private void updateView(){
        notificationCheckBox.setChecked(showNotification);
        vibrationCheckBox.setChecked(vibrate);
        frequencySpinner.setSelection(selectedFrequency);

        Log.d(TAG+"SEL", "SSS - "+selectedFrequency);
    }

    private void initViews(boolean isAmoled){
        frequencyArray = getResources().getStringArray(R.array.updater_background_frequency_entries);
        if(isAmoled) {
            setContentView(R.layout.activity_settings_amoled);
            qrequencyAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row_item_amoled, R.id.spinner_row_text, frequencyArray);
        } else {
            setContentView(R.layout.activity_settings);
            qrequencyAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row_item, R.id.spinner_row_text, frequencyArray);
        }
        frequencySpinner = (Spinner) findViewById(R.id.update_period_frequency_spinner);
        frequencySpinner.setSelection(selectedFrequency);
        notificationCheckBox = (CheckBox) findViewById(R.id.show_notification_switch);
        vibrationCheckBox = (CheckBox) findViewById(R.id.vibration_switch);
        notificationSoundBtn = (Button) findViewById(R.id.select_melody_btn);
    }
}
