package com.platypii.avyalert;

import com.platypii.avyalert.R;
import com.platypii.avyalert.regions.Regions;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);       
        addPreferencesFromResource(R.xml.preferences);
        
        Preference pushPref = findPreference("enable_push");
        pushPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(SettingsActivity.this, "Push notifications is a paid feature", Toast.LENGTH_SHORT).show();
                // TODO: In-app billing
                return false;
            }
        });
        
        ListPreference regionPref = (ListPreference) findPreference("region");
        CharSequence regions[] = Regions.getRegionNames();
        regionPref.setEntries(regions);
        regionPref.setEntryValues(regions);
        regionPref.setDefaultValue(regions[0]);
        regionPref.setEnabled(true);

    }
}
