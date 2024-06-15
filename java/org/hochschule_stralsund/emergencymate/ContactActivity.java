package org.hochschule_stralsund.emergencymate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactActivity extends AppCompatActivity {

    private EditText contactName;
    private EditText contactPhone;
    private Button addContactButton;
    private ListView contactsListView;
    private List<String> contactList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        contactName = findViewById(R.id.contactName);
        contactPhone = findViewById(R.id.contactPhone);
        addContactButton = findViewById(R.id.addContactButton);
        contactsListView = findViewById(R.id.contactsListView);

        contactList = loadContacts();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        contactsListView.setAdapter(adapter);

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });
    }

    private void addContact() {
        String name = contactName.getText().toString().trim();
        String phone = contactPhone.getText().toString().trim();
        if (!name.isEmpty() && !phone.isEmpty()) {
            String contact = name + " - " + phone;
            contactList.add(contact);
            adapter.notifyDataSetChanged();
            saveContacts();
            contactName.setText("");
            contactPhone.setText("");
            Toast.makeText(this, "Kontakt hinzugefügt", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bitte Namen und Telefonnummer eingeben", Toast.LENGTH_SHORT).show();
        }
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

    private void saveContacts() {
        String activeProfile = getSharedPreferences("activeProfile", MODE_PRIVATE).getString("profileName", null);
        if (activeProfile != null) {
            JSONArray jsonArray = new JSONArray();
            for (String contact : contactList) {
                try {
                    JSONObject contactObject = new JSONObject();
                    String[] parts = contact.split(" - ");
                    contactObject.put("name", parts[0]);
                    contactObject.put("phone", parts[1]);
                    jsonArray.put(contactObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SharedPreferences.Editor editor = getSharedPreferences(activeProfile + "_contactsPrefs", MODE_PRIVATE).edit();
            editor.putString("contacts", jsonArray.toString());
            editor.apply();
        }
    }
}
