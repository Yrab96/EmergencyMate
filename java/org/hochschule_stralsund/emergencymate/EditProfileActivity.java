package org.hochschule_stralsund.emergencymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editProfileNameEditText;
    private EditText editAddressEditText;

    private String originalProfileName;
    private String originalAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileNameEditText = findViewById(R.id.editProfileNameEditText);
        editAddressEditText = findViewById(R.id.editAddressEditText);

        Intent intent = getIntent();
        if (intent != null) {
            String profileName = intent.getStringExtra("profileName");
            if (profileName != null) {
                // Load current profile data for the selected profile
                SharedPreferences profilePrefs = getSharedPreferences(profileName + "_profilePrefs", MODE_PRIVATE);
                originalProfileName = profileName;
                originalAddress = profilePrefs.getString("address", "");

                editProfileNameEditText.setText(originalProfileName);
                editAddressEditText.setText(originalAddress);
            }
        }
    }

    public void saveProfile(View view) {
        final String profileName = editProfileNameEditText.getText().toString().trim();
        final String address = editAddressEditText.getText().toString().trim();

        if (profileName.isEmpty()) {
            Toast.makeText(this, "Profilname darf nicht leer sein", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Profil speichern")
                .setMessage("Sind Sie sicher, dass Sie die Änderungen speichern möchten?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Save profile to specific profilePrefs
                    SharedPreferences profilePrefs = getSharedPreferences(originalProfileName + "_profilePrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editorProfilePrefs = profilePrefs.edit();
                    editorProfilePrefs.putString("address", address);
                    editorProfilePrefs.apply();

                    Toast.makeText(EditProfileActivity.this, "Profil gespeichert", Toast.LENGTH_SHORT).show();

                    // Finish EditProfileActivity
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
