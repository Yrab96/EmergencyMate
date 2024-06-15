package org.hochschule_stralsund.emergencymate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MedicalInfoActivity extends AppCompatActivity {

    private EditText allergiesEditText;
    private EditText medicationsEditText;
    private EditText chronicConditionsEditText;
    private EditText bloodTypeEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_info);

        allergiesEditText = findViewById(R.id.allergiesEditText);
        medicationsEditText = findViewById(R.id.medicationsEditText);
        chronicConditionsEditText = findViewById(R.id.chronicConditionsEditText);
        bloodTypeEditText = findViewById(R.id.bloodTypeEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMedicalInfo();
            }
        });

        loadMedicalInfo();
    }

    private void saveMedicalInfo() {
        SharedPreferences prefs = getSharedPreferences("activeProfile", MODE_PRIVATE);
        String activeProfile = prefs.getString("profileName", null);

        if (activeProfile != null) {
            SharedPreferences medicalInfoPrefs = getSharedPreferences(activeProfile + "_medicalInfoPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = medicalInfoPrefs.edit();

            editor.putString("allergies", allergiesEditText.getText().toString().trim());
            editor.putString("medications", medicationsEditText.getText().toString().trim());
            editor.putString("chronicConditions", chronicConditionsEditText.getText().toString().trim());
            editor.putString("bloodType", bloodTypeEditText.getText().toString().trim());

            editor.apply();

            Toast.makeText(this, "Medizinische Informationen gespeichert", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadMedicalInfo() {
        SharedPreferences prefs = getSharedPreferences("activeProfile", MODE_PRIVATE);
        String activeProfile = prefs.getString("profileName", null);

        if (activeProfile != null) {
            SharedPreferences medicalInfoPrefs = getSharedPreferences(activeProfile + "_medicalInfoPrefs", MODE_PRIVATE);

            allergiesEditText.setText(medicalInfoPrefs.getString("allergies", ""));
            medicationsEditText.setText(medicalInfoPrefs.getString("medications", ""));
            chronicConditionsEditText.setText(medicalInfoPrefs.getString("chronicConditions", ""));
            bloodTypeEditText.setText(medicalInfoPrefs.getString("bloodType", ""));
        }
    }
}

