package com.example.bridgebidirectionalhunt;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CallReceiver extends BroadcastReceiver {

    private static final long DELAY_MILLIS = 5000; // 5 seconds

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String number = getLastCallDetails(context);
                if (number != null) {
                    String callType = getCallType(context);
                    sendSMS(context, number, callType);
                }
            }, DELAY_MILLIS);
        }
    }

    @SuppressLint("Range")
    private String getLastCallDetails(Context context) {
        String lastNumber = null;
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            lastNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            cursor.close();
        }
        return lastNumber;
    }

    @SuppressLint("Range")
    private String getCallType(Context context) {
        String callType = null;
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            switch (type) {
                case CallLog.Calls.MISSED_TYPE:
                    callType = "missedCallMessage";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "answeredCallMessage";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "outgoingCallMessage";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    callType = "redirectedCallMessage";
                    break;
            }
            cursor.close();
        }
        return callType;
    }

    private void sendSMS(Context context, String phoneNumber, String callTypeKey) {
        android.content.SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        // Vérifier si le numéro a déjà reçu un SMS
        if (allEntries.containsKey(phoneNumber) || isNumberInContacts(context, phoneNumber)) {
            Toast.makeText(context, phoneNumber + " " + context.getString(R.string.already_sms), Toast.LENGTH_LONG).show();
            return;
        }

        // Récupérer le message de réponse automatique
        String message = preferences.getString(callTypeKey, null);

        if (message != null) {
            // Envoyer le SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Enregistrer le numéro, le message et la date dans SharedPreferences
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            JSONObject messageInfo = new JSONObject();
            try {
                messageInfo.put("message", message);
                messageInfo.put("date", currentDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            preferences.edit().putString(phoneNumber, messageInfo.toString()).apply();

            // Afficher un Toast pour informer l'utilisateur que le SMS a été envoyé
            Toast.makeText(context, context.getString(R.string.sms_sent_to) + phoneNumber, Toast.LENGTH_LONG).show();

            // Créer une notification après l'envoi du SMS
            showNotification(context, phoneNumber, true);
        } else {
            // Créer une notification si le SMS de réponse automatique n'a pas été configuré
            showNotification(context, phoneNumber, false);
        }
    }


    @SuppressLint("NotificationPermission")
    private void showNotification(Context context, String phoneNumber, boolean isSmsSent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "sms_sent_channel";

        // Créer un canal de notification si nécessaire (Android 8.0 et plus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "SMS Sent",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_logo_white)
                .setContentTitle(context.getString(isSmsSent ? R.string.sms_sent : R.string.sms_sent_error))
                .setContentText(isSmsSent ? context.getString(R.string.sms_sent_to) + phoneNumber : context.getString(R.string.sms_sent_to))
                .setPriority(NotificationCompat.PRIORITY_MAX);


        // Afficher la notification
        notificationManager.notify(1, builder.build());
    }

    private boolean isNumberInContacts(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = ((ContentResolver) contentResolver).query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        }

        return false;
    }
}
