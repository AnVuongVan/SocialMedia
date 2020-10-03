package com.vietis.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String TOPIC_POST_NOTIFICATION = "POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        SwitchCompat postSwitch = findViewById(R.id.postSwitch);
        sharedPreferences = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sharedPreferences.getBoolean(TOPIC_POST_NOTIFICATION, false);

        if (isPostEnabled) {
            postSwitch.setChecked(true);
        } else {
            postSwitch.setChecked(false);
        }

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                editor = sharedPreferences.edit();
                editor.putBoolean(TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked) {
                    subscribePostNotification();
                } else {
                    unSubscribePostNotification();
                }
            }
        });

    }

    private void unSubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You won't receive post notifications";
                        if (!task.isSuccessful()) {
                            msg = "UnSubscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notifications";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}