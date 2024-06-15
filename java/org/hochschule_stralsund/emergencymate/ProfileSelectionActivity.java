package org.hochschule_stralsund.emergencymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ProfileSelectionActivity extends AppCompatActivity {

    private ListView profileListView;
    private List<String> profileList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_selection);

        profileListView = findViewById(R.id.profileListView);

        profileList = loadProfiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profileList);
        profileListView.setAdapter(adapter);

        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedProfile = profileList.get(position);
                setActiveProfile(selectedProfile);
            }
        });
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

    private void setActiveProfile(String profileName) {
        SharedPreferences.Editor editor = getSharedPreferences("activeProfile", MODE_PRIVATE).edit();
        editor.putString("profileName", profileName);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Beenden der ProfileSelectionActivity nach Auswahl des Profils
    }
}
