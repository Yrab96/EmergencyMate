package org.hochschule_stralsund.emergencymate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private EditText profileNameEditText;
    private EditText addressEditText;
    private Button createProfileButton;
    private ListView profileListView;
    private TextView hintTextView;
    private List<String> profileList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileNameEditText = findViewById(R.id.profileNameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        createProfileButton = findViewById(R.id.createProfileButton);
        profileListView = findViewById(R.id.profileListView);
        hintTextView = findViewById(R.id.hintTextView);

        profileList = loadProfiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profileList);
        profileListView.setAdapter(adapter);

        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile();
            }
        });

        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedProfile = profileList.get(position);
                openEditProfileActivity(selectedProfile);
            }
        });

        profileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmation(position);
                return true; // Consume the long click
            }
        });
    }

    private void createProfile() {
        String profileName = profileNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        if (!profileName.isEmpty() && !address.isEmpty() && !profileList.contains(profileName)) {
            profileList.add(profileName);
            adapter.notifyDataSetChanged();
            saveProfiles(profileName, address);
            profileNameEditText.setText("");
            addressEditText.setText("");
            Toast.makeText(this, "Profil erstellt", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Profilname oder Adresse ist leer oder existiert bereits", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> loadProfiles() {
        List<String> profiles = new ArrayList<>();
        String profilesString = getSharedPreferences("profilesPrefs", MODE_PRIVATE).getString("profiles", "[]");
        try {
            JSONArray jsonArray = new JSONArray(profilesString);
            for (int i = 0; i < jsonArray.length(); i++) {
                profiles.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    private void saveProfiles(String profileName, String address) {
        JSONArray jsonArray = new JSONArray();
        for (String profile : profileList) {
            jsonArray.put(profile);
        }
        SharedPreferences.Editor editor = getSharedPreferences("profilesPrefs", MODE_PRIVATE).edit();
        editor.putString("profiles", jsonArray.toString());
        editor.apply();

        SharedPreferences profilePrefs = getSharedPreferences(profileName + "_profilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor profileEditor = profilePrefs.edit();
        profileEditor.putString("address", address);
        profileEditor.apply();
    }

    private void openEditProfileActivity(String profileName) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("profileName", profileName);
        startActivity(intent);
    }

    private void showDeleteConfirmation(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profil löschen");
        builder.setMessage("Möchten Sie dieses Profil wirklich löschen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String profileToDelete = profileList.get(position);
                deleteProfile(profileToDelete);
            }
        });
        builder.setNegativeButton("Nein", null);
        builder.show();
    }

    // Methode zum Löschen eines Profils
    public void deleteProfile(String profileName) {
        SharedPreferences.Editor editor = getSharedPreferences("profilesPrefs", MODE_PRIVATE).edit();
        List<String> updatedProfileList = new ArrayList<>(profileList);
        updatedProfileList.remove(profileName);
        JSONArray jsonArray = new JSONArray(updatedProfileList);
        editor.putString("profiles", jsonArray.toString());
        editor.apply();

        SharedPreferences profilePrefs = getSharedPreferences(profileName + "_profilePrefs", MODE_PRIVATE);
        profilePrefs.edit().clear().apply();

        profileList.remove(profileName);
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Profil '" + profileName + "' gelöscht", Toast.LENGTH_SHORT).show();
    }
}
