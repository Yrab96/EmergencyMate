package org.hochschule_stralsund.emergencymate;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1;
    private static final int REQUEST_CALL_PERMISSION = 2;
    private TextView profileSummaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileSummaryTextView = findViewById(R.id.profileSummaryTextView);

        // Setup "Profil wählen" Button in der Toolbar
        findViewById(R.id.selectProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Überprüfen und aktualisieren der Profilzusammenfassung
        updateProfileSummary();

        // Berechtigungen überprüfen
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileSummary(); // Profilzusammenfassung aktualisieren, wenn zur MainActivity zurückgekehrt wird
    }

    private void updateProfileSummary() {
        SharedPreferences activeProfilePrefs = getSharedPreferences("activeProfile", MODE_PRIVATE);
        String profileName = activeProfilePrefs.getString("profileName", "");

        if (!profileName.isEmpty()) {
            SharedPreferences profilePrefs = getSharedPreferences(profileName + "_profilePrefs", MODE_PRIVATE);
            SharedPreferences medicalInfoPrefs = getSharedPreferences(profileName + "_medicalInfoPrefs", MODE_PRIVATE);

            String address = profilePrefs.getString("address", "Keine Adresse");
            String allergies = medicalInfoPrefs.getString("allergies", "Keine Allergien");
            String medications = medicalInfoPrefs.getString("medications", "Keine Medikamente");
            String chronicConditions = medicalInfoPrefs.getString("chronicConditions", "Keine chronischen Krankheiten");
            String bloodType = medicalInfoPrefs.getString("bloodType", "Keine Blutgruppe");
            Set<String> selectedContacts = getSharedPreferences(profileName + "_sosPrefs", MODE_PRIVATE).getStringSet("selectedContacts", null);

            StringBuilder contactsSummary = new StringBuilder();
            if (selectedContacts != null && !selectedContacts.isEmpty()) {
                for (String contact : selectedContacts) {
                    contactsSummary.append(contact).append("\n");
                }
            } else {
                contactsSummary.append("Keine SOS-Kontakte ausgewählt");
            }

            SpannableStringBuilder summary = new SpannableStringBuilder();

            // Helper method to create styled text
            summary.append(createStyledText("Profilname: ", profileName));
            summary.append("\n");
            summary.append(createStyledText("Adresse: ", address));
            summary.append("\n");
            summary.append(createStyledText("Allergien: ", allergies));
            summary.append("\n");
            summary.append(createStyledText("Medikamente: ", medications));
            summary.append("\n");
            summary.append(createStyledText("Chronische Krankheiten: ", chronicConditions));
            summary.append("\n");
            summary.append(createStyledText("Blutgruppe: ", bloodType));
            summary.append("\n");
            summary.append(createStyledText("SOS-Kontakte: \n", contactsSummary.toString()));

            profileSummaryTextView.setText(summary);
        } else {
            profileSummaryTextView.setText("Kein Profil ausgewählt");
        }
    }

    private SpannableStringBuilder createStyledText(String title, String content) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        // Create and style title part
        SpannableString titleSpannable = new SpannableString(title);
        titleSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleSpannable.setSpan(new UnderlineSpan(), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create content part
        SpannableString contentSpannable = new SpannableString(content);

        // Append both parts to the builder
        spannableStringBuilder.append(titleSpannable);
        spannableStringBuilder.append(contentSpannable);

        return spannableStringBuilder;
    }

    // Methode zum Überprüfen und Anfordern von Berechtigungen
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    // Methoden für die Navigation zu anderen Activities
    public void openProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openContacts(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    public void openMedicalInfo(View view) {
        Intent intent = new Intent(this, MedicalInfoActivity.class);
        startActivity(intent);
    }

    public void openSOS(View view) {
        Intent intent = new Intent(this, SOSActivity.class);
        startActivity(intent);
    }

    public void sendSOS(View view) {
        String activeProfile = getSharedPreferences("activeProfile", MODE_PRIVATE).getString("profileName", null);
        if (activeProfile != null) {
            SharedPreferences sosPrefs = getSharedPreferences(activeProfile + "_sosPrefs", MODE_PRIVATE);
            boolean notifyEmergency = sosPrefs.getBoolean("notifyEmergency", false);
            Set<String> selectedContacts = sosPrefs.getStringSet("selectedContacts", null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bestätigung");
            builder.setMessage("Möchten Sie wirklich eine SOS-Benachrichtigung an Ihre Kontakte senden?");
            builder.setPositiveButton("Ja", (dialog, which) -> {
                if (notifyEmergency) {
                    callEmergency(); // Notruf an 112 tätigen
                }

                if (selectedContacts != null && !selectedContacts.isEmpty()) {
                    for (String contact : selectedContacts) {
                        String[] parts = contact.split(" - ");
                        if (parts.length == 2) {
                            String contactPhone = parts[1];
                            sendSMS(contactPhone); // SMS an Notfallkontakt senden
                        }
                    }
                } else {
                    Toast.makeText(this, "Keine SOS-Kontakte ausgewählt", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Nein", null);
            builder.show();
        }
    }



    private void sendSMS(String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, "SOS! Ich brauche Hilfe!", null, null);
        Toast.makeText(this, "SOS-Nachricht an Notfallkontakt gesendet", Toast.LENGTH_SHORT).show();
    }

    private void callEmergency() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:112"));
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler beim Tätigen des Notrufs. Bitte überprüfen Sie die Berechtigungen.", Toast.LENGTH_SHORT).show();
        }
    }
}
