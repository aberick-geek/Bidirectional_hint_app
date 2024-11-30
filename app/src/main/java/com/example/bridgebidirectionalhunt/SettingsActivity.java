package com.example.bridgebidirectionalhunt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_AUTO_REPLY_MESSAGE = "autoReplyMessage";
    private static final int PERMISSION_REQUEST_CODE = 1;

    // EditTexts and Buttons for different types of auto-reply messages
    private EditText smsReceivedEditText, missedCallEditText, answeredCallEditText, redirectedCallEditText;
    private Button smsReceivedSaveButton, missedCallSaveButton, answeredCallSaveButton, redirectedCallSaveButton;
    private LinearLayout smsReceivedBubble, missedCallBubble, answeredCallBubble, redirectedCallBubble;
    private TextView smsReceivedTextView, missedCallTextView, answeredCallTextView, redirectedCallTextView;
    private TextView smsReceivedDateTextView, missedCallDateTextView, answeredCallDateTextView, redirectedCallDateTextView;
    private ImageButton smsReceivedDeleteButton, missedCallDeleteButton, answeredCallDeleteButton, redirectedCallDeleteButton;
    private SharedPreferences preferences;
    private Snackbar smsReceivedSnackbar;
    private Snackbar missedCallSnackbar;
    private Snackbar answeredCallSnackbar;
    private Snackbar redirectedCallSnackbar;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        // Trouver le bouton des paramètres
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.btn_settings);

        // Ajouter un écouteur de clic pour naviguer vers MainActivity2
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HistoryActivity.class);
            startActivity(intent);
        });


        // Initialize views for SMS received
        smsReceivedEditText = findViewById(R.id.smsReceivedEditText);
        smsReceivedSaveButton = findViewById(R.id.smsReceivedSaveButton);
        smsReceivedBubble = findViewById(R.id.smsReceivedBubble);
        smsReceivedTextView = findViewById(R.id.smsReceivedTextView);
        smsReceivedDateTextView = findViewById(R.id.smsReceivedDateTextView);
        smsReceivedDeleteButton = findViewById(R.id.smsReceivedDeleteButton);

        // Initialize views for missed calls
        missedCallEditText = findViewById(R.id.missedCallEditText);
        missedCallSaveButton = findViewById(R.id.missedCallSaveButton);
        missedCallBubble = findViewById(R.id.missedCallBubble);
        missedCallTextView = findViewById(R.id.missedCallTextView);
        missedCallDateTextView = findViewById(R.id.missedCallDateTextView);
        missedCallDeleteButton = findViewById(R.id.missedCallDeleteButton);

        // Initialize views for answered calls
        answeredCallEditText = findViewById(R.id.answeredCallEditText);
        answeredCallSaveButton = findViewById(R.id.answeredCallSaveButton);
        answeredCallBubble = findViewById(R.id.answeredCallBubble);
        answeredCallTextView = findViewById(R.id.answeredCallTextView);
        answeredCallDateTextView = findViewById(R.id.answeredCallDateTextView);
        answeredCallDeleteButton = findViewById(R.id.answeredCallDeleteButton);

        // Initialize views for redirected calls
        redirectedCallEditText = findViewById(R.id.redirectedCallEditText);
        redirectedCallSaveButton = findViewById(R.id.redirectedCallSaveButton);
        redirectedCallBubble = findViewById(R.id.redirectedCallBubble);
        redirectedCallTextView = findViewById(R.id.redirectedCallTextView);
        redirectedCallDateTextView = findViewById(R.id.redirectedCallDateTextView);
        redirectedCallDeleteButton = findViewById(R.id.redirectedCallDeleteButton);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Vérifier les configurations d'auto-réponse
        checkAutoReplyConfigurations();

        // Load saved messages and update UI
        loadSavedMessages();

        // Set save button click listeners
        smsReceivedSaveButton.setOnClickListener(v -> saveMessage("smsReceived", smsReceivedEditText));
        missedCallSaveButton.setOnClickListener(v -> saveMessage("missedCall", missedCallEditText));
        answeredCallSaveButton.setOnClickListener(v -> saveMessage("answeredCall", answeredCallEditText));
        redirectedCallSaveButton.setOnClickListener(v -> saveMessage("redirectedCall", redirectedCallEditText));

        // Set delete button click listeners
        smsReceivedDeleteButton.setOnClickListener(v -> deleteMessage("smsReceived"));
        missedCallDeleteButton.setOnClickListener(v -> deleteMessage("missedCall"));
        answeredCallDeleteButton.setOnClickListener(v -> deleteMessage("answeredCall"));
        redirectedCallDeleteButton.setOnClickListener(v -> deleteMessage("redirectedCall"));

        // Check if auto-reply message is configured
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String autoReplyMessage = preferences.getString(KEY_AUTO_REPLY_MESSAGE, null);

        // Vérification et demande de permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void saveMessage(String key, EditText editText) {
        String message = editText.getText().toString();
        if (!message.isEmpty()) {
            // Save the message and current date
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key + "Message", message);
            editor.putString(key + "Date", getCurrentDate());
            editor.apply();

            // Update the UI
            loadSavedMessages();

            Toast.makeText(SettingsActivity.this, "Message saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMessage(String key) {
        // Remove the saved message
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key + "Message");
        editor.remove(key + "Date");
        editor.apply();

        // Update the UI
        switch (key) {
            case "smsReceived":
                smsReceivedEditText.setText("");
                smsReceivedBubble.setVisibility(View.GONE);
                smsReceivedEditText.setVisibility(View.VISIBLE);
                smsReceivedSaveButton.setVisibility(View.VISIBLE);
                break;
            case "missedCall":
                missedCallEditText.setText("");
                missedCallBubble.setVisibility(View.GONE);
                missedCallEditText.setVisibility(View.VISIBLE);
                missedCallSaveButton.setVisibility(View.VISIBLE);
                break;
            case "answeredCall":
                answeredCallEditText.setText("");
                answeredCallBubble.setVisibility(View.GONE);
                answeredCallEditText.setVisibility(View.VISIBLE);
                answeredCallSaveButton.setVisibility(View.VISIBLE);
                break;
            case "redirectedCall":
                redirectedCallEditText.setText("");
                redirectedCallBubble.setVisibility(View.GONE);
                redirectedCallEditText.setVisibility(View.VISIBLE);
                redirectedCallSaveButton.setVisibility(View.VISIBLE);
                break;
        }

        Toast.makeText(SettingsActivity.this, "Message deleted!", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedMessages() {
        loadSavedMessage("smsReceived", smsReceivedBubble, smsReceivedEditText, smsReceivedSaveButton, smsReceivedTextView, smsReceivedDateTextView);
        loadSavedMessage("missedCall", missedCallBubble, missedCallEditText, missedCallSaveButton, missedCallTextView, missedCallDateTextView);
        loadSavedMessage("answeredCall", answeredCallBubble, answeredCallEditText, answeredCallSaveButton, answeredCallTextView, answeredCallDateTextView);
        loadSavedMessage("redirectedCall", redirectedCallBubble, redirectedCallEditText, redirectedCallSaveButton, redirectedCallTextView, redirectedCallDateTextView);
    }

    private void loadSavedMessage(String key, LinearLayout bubble, EditText editText, Button saveButton, TextView textView, TextView dateTextView) {
        String savedMessage = preferences.getString(key + "Message", null);
        String savedDate = preferences.getString(key + "Date", null);

        if (savedMessage != null) {
            // Show the message bubble and hide input fields
            bubble.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);

            // Set the message and date in the bubble
            textView.setText(savedMessage);
            dateTextView.setText(getString(R.string.update_at) + savedDate);
        } else {
            // Show the input fields and hide the message bubble
            bubble.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void checkAutoReplyConfigurations() {
        // Vérifier la configuration pour SMS reçus
        String smsReceivedMessage = preferences.getString("smsReceivedMessage", null);
        if (smsReceivedMessage == null) {
            smsReceivedSnackbar = Snackbar.make(findViewById(android.R.id.content),
                            R.string.Auto_reply_not_set_incoming_SMS, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.set_auto_reply, v -> {
                        // Naviguer vers la page de configuration des SMS reçus
                        // Par exemple : openSettingsPage("SMS_RECEIVED");
                    });
            smsReceivedSnackbar.show();
        }

        // Vérifier la configuration pour appels manqués
        String missedCallMessage = preferences.getString("missedCallMessage", null);
        if (missedCallMessage == null) {
            missedCallSnackbar = Snackbar.make(findViewById(android.R.id.content),
                            R.string.Auto_reply_not_set_missed_calls, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.set_auto_reply, v -> {
                        // Naviguer vers la page de configuration des appels manqués
                        // Par exemple : openSettingsPage("MISSED_CALL");
                    });
            missedCallSnackbar.show();
        }

        // Vérifier la configuration pour appels répondus
        String answeredCallMessage = preferences.getString("answeredCallMessage", null);
        if (answeredCallMessage == null) {
            answeredCallSnackbar = Snackbar.make(findViewById(android.R.id.content),
                            R.string.Auto_reply_not_set_answered_calls, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.set_auto_reply, v -> {
                        // Naviguer vers la page de configuration des appels répondus
                        // Par exemple : openSettingsPage("ANSWERED_CALL");
                    });
            answeredCallSnackbar.show();
        }

        // Vérifier la configuration pour appels redirigés
        String redirectedCallMessage = preferences.getString("redirectedCallMessage", null);
        if (redirectedCallMessage == null) {
            redirectedCallSnackbar = Snackbar.make(findViewById(android.R.id.content),
                            R.string.Auto_reply_not_set_redirected_calls, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.set_auto_reply, v -> {
                        // Naviguer vers la page de configuration des appels redirigés
                        // Par exemple : openSettingsPage("REDIRECTED_CALL");
                    });
            redirectedCallSnackbar.show();
        }
    }

}
