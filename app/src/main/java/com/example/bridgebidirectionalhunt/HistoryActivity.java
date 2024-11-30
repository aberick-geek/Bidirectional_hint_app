package com.example.bridgebidirectionalhunt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

public class HistoryActivity extends AppCompatActivity {
    private LinearLayout messageContainer;
    private LinearLayout emptyStateContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Trouver le bouton pour revenir en arrière
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.btn_go_back);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton exportButton = findViewById(R.id.btn_export);

        // Ajouter un écouteur de clic pour naviguer vers SettingsActivity
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        exportButton.setOnClickListener(v -> exportToExcel());

        // Références aux vues
        messageContainer = findViewById(R.id.messageContainer);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        boolean hasPhoneNumbers = false;

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("smsReceivedMessage") &&
                    !key.equals("smsReceivedDate") &&
                    !key.equals("missedCallMessage") &&
                    !key.equals("missedCallDate") &&
                    !key.equals("answeredCallMessage") &&
                    !key.equals("answeredCallDate") &&
                    !key.equals("redirectedCallMessage") &&
                    !key.equals("redirectedCallDate")) {

                // There is at least one phone number stored
                hasPhoneNumbers = true;
                emptyStateContainer.setVisibility(View.GONE);

                // Parse the JSON string to retrieve the message and date
                String jsonString = entry.getValue().toString();
                try {
                    JSONObject messageInfo = new JSONObject(jsonString);
                    String message = messageInfo.getString("message");
                    String date = messageInfo.getString("date");

                    // Log the phone number, message, and date

                    // Inflation du layout de la bulle
                    View bubbleView = LayoutInflater.from(this).inflate(R.layout.item_message_bubble, messageContainer, false);

                    TextView dateTextView = bubbleView.findViewById(R.id.dateTextView);
                    TextView phoneNumberTextView = bubbleView.findViewById(R.id.phoneNumberTextView);
                    TextView messageTextView = bubbleView.findViewById(R.id.messageTextView);

                    dateTextView.setText(date);
                    phoneNumberTextView.setText(key);
                    messageTextView.setText(message);

                    messageContainer.addView(bubbleView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        if (!hasPhoneNumbers) {
            // If no phone numbers are found, log an "empty" message
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
    }

    private void exportToExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("History");

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Phone Number");

        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Message");

        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("Date");

        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("smsReceivedMessage") &&
                    !key.equals("smsReceivedDate") &&
                    !key.equals("missedCallMessage") &&
                    !key.equals("missedCallDate") &&
                    !key.equals("answeredCallMessage") &&
                    !key.equals("answeredCallDate") &&
                    !key.equals("redirectedCallMessage") &&
                    !key.equals("redirectedCallDate")) {

                // Parse the JSON string to retrieve the message and date
                String jsonString = entry.getValue().toString();
                try {
                    JSONObject messageInfo = new JSONObject(jsonString);
                    String message = messageInfo.getString("message");
                    String date = messageInfo.getString("date");

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(key);
                    row.createCell(1).setCellValue(message);
                    row.createCell(2).setCellValue(date);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            File file = new File(getExternalFilesDir(null), "history.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

            Log.d("HistoryActivity", "Excel file exported: " + file.getAbsolutePath());
            Toast.makeText(this, "Export successful! File saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Appeler la méthode pour ouvrir ou partager le fichier
            shareOrOpenExcel(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shareOrOpenExcel(File file) {
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Utilisation de FileProvider pour les versions d'Android N (API 24) ou supérieures
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Proposer à l'utilisateur de choisir une application pour ouvrir ou partager le fichier
            Intent chooser = Intent.createChooser(intent, "Ouvrir ou partager le fichier Excel");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                Toast.makeText(this, "Aucune application disponible pour ouvrir ce fichier", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du partage ou de l'ouverture du fichier", Toast.LENGTH_SHORT).show();
        }
    }


}
