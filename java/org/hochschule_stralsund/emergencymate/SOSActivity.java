package org.hochschule_stralsund.emergencymate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SOSActivity extends AppCompatActivity {

    private ListView contactsListView;
    private Button saveButton;
    private CheckBox notifyEmergencyCheckBox;
    private List<String> contactList;
    private Set<String> selectedContacts = new HashSet<>();
    private boolean notifyEmergency = false; // Flag to track if emergency notification should be sent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        contactsListView = findViewById(R.id.contactsListView);
        saveButton = findViewById(R.id.saveButton);
        notifyEmergencyCheckBox = findViewById(R.id.notifyEmergencyCheckBox);

        contactList = loadContacts();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, contactList);
        contactsListView.setAdapter(adapter);
        contactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedContacts();
            }
        });

        notifyEmergencyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notifyEmergency = isChecked;
        });

        loadSelectedContacts();
    }

    private List<String> loadContacts() {
        List<String> contacts = new ArrayList<>();
        String activeProfile = getSharedPreferences("activeProfile", MODE_PRIVATE).getString("profileName", null);
        if (activeProfile != null) {
            String contactsString = getSharedPreferences(activeProfile + "_contactsPrefs", MODE_PRIVATE).getString("contacts", "[]");
            try {
                JSONArray jsonArray = new JSONArray(contactsString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contact = jsonArray.getJSONObject(i);
                    String contactInfo = contact.getString("name") + " - " + contact.getString("phone");
                    contacts.add(contactInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return contacts;
    }

    private void saveSelectedContacts() {
        SparseBooleanArray checked = contactsListView.getCheckedItemPositions();
        selectedContacts.clear();
        for (int i = 0; i < contactsListView.getCount(); i++) {
            if (checked.get(i)) {
                selectedContacts.add(contactList.get(i));
            }
        }

        String activeProfile = getSharedPreferences("activeProfile", MODE_PRIVATE).getString("profileName", null);
        if (activeProfile != null) {
            SharedPreferences.Editor editor = getSharedPreferences(activeProfile + "_sosPrefs", MODE_PRIVATE).edit();
            editor.putStringSet("selectedContacts", selectedContacts);
            editor.putBoolean("notifyEmergency", notifyEmergency); // Save the state of emergency notification
            editor.apply();

            Toast.makeText(this, "SOS-Kontakte gespeichert", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSelectedContacts() {
        String activeProfile = getSharedPreferences("activeProfile", MODE_PRIVATE).getString("profileName", null);
        if (activeProfile != null) {
            Set<String> savedContacts = getSharedPreferences(activeProfile + "_sosPrefs", MODE_PRIVATE).getStringSet("selectedContacts", new HashSet<String>());
            for (int i = 0; i < contactsListView.getCount(); i++) {
                if (savedContacts.contains(contactList.get(i))) {
                    contactsListView.setItemChecked(i, true);
                }
            }

            notifyEmergency = getSharedPreferences(activeProfile + "_sosPrefs", MODE_PRIVATE).getBoolean("notifyEmergency", false);
            notifyEmergencyCheckBox.setChecked(notifyEmergency);
        }
    }
}
