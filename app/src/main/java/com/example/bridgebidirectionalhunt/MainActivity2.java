
package com.example.bridgebidirectionalhunt;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity2 extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_AUTO_REPLY_MESSAGE = "autoReplyMessage";
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Trouver le bouton des paramètres
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.btn_settings);

        // Ajouter un écouteur de clic pour naviguer vers MainActivity2
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Check if auto-reply message is configured
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String autoReplyMessage = preferences.getString(KEY_AUTO_REPLY_MESSAGE, null);

        // Show Snackbar if auto-reply message is not configured
        if (autoReplyMessage == null || autoReplyMessage.isEmpty()) {
            showSnackbar();
        }

        // Vérification et demande de permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS},
                    1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE},
                    1);
        }

        // Check if the permissions are already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_CODE);
        }
    }
    private void showSnackbar() {
        View rootView = findViewById(android.R.id.content); // Get the root view of the activity
        Snackbar.make(rootView, "SMS de réponse automatique non configuré", Snackbar.LENGTH_LONG)
                .setAction("Configurer", v -> {
                    // Handle the action if the user clicks "Configurer"
                    // For example, you can navigate to the settings activity
                    Intent intent = new Intent(MainActivity2.this, SettingsActivity.class);
                    startActivity(intent);
                })
                .show();
    }
}