package com.example.SmartTerrariumAplikacjaMobilna;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import java.util.Locale;

public class ChangeLanguageActivity extends Activity {

    private Spinner languageSpinner;
    private Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);

        languageSpinner = findViewById(R.id.language_spinner);
        applyButton = findViewById(R.id.apply_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedLanguagePosition = languageSpinner.getSelectedItemPosition();
                String selectedLanguageCode = getResources().getStringArray(R.array.languages_values)[selectedLanguagePosition];
                changeLanguage(selectedLanguageCode);
            }
        });

    }

    private void changeLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;

        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Zapisz wybrany język do SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();

        // Restartuj bieżącą aktywność
        recreate();
    }
}
