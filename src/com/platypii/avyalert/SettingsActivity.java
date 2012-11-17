package com.platypii.avyalert;

import com.platypii.avyalert.R;
import com.platypii.avyalert.billing.BillingActivity;
import com.platypii.avyalert.regions.Regions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);       
        addPreferencesFromResource(R.xml.preferences);

        Preference pushPref = findPreference("enablePush");
        pushPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // TODO: Check subscription status
                final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                boolean isSubscribed = prefs.getBoolean("isSubscribed", false);
                if(isSubscribed) {
                    // Enable
                    return true;
                } else {
                    // Open BillingActivity
                    startActivity(new Intent(SettingsActivity.this, BillingActivity.class));
                    // Toast.makeText(SettingsActivity.this, "Push notifications is a paid feature", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
        
        ListPreference regionPref = (ListPreference) findPreference("currentRegion");
        CharSequence regions[] = Regions.getRegionNames();
        regionPref.setEntries(regions);
        regionPref.setEntryValues(regions);
        regionPref.setDefaultValue(regions[0]);
        regionPref.setEnabled(true);

    }
}
