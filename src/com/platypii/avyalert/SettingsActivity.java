package com.platypii.avyalert;

import com.platypii.avyalert.R;
import com.platypii.avyalert.data.Regions;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);       
        addPreferencesFromResource(R.xml.preferences);
        
        if(android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(true);
        }

        if(Debug.ENABLE_NOTIFICATIONS) {
            findPreference("enablePush").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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
                        // TODO: startActivity(new Intent(SettingsActivity.this, BillingActivity.class));
                        Toast.makeText(SettingsActivity.this, "Push notifications is a paid feature", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            });
        }
        
        ListPreference regionPref = (ListPreference) findPreference("currentRegion");
        CharSequence regions[] = Regions.getRegionNames();
        regionPref.setEntries(regions);
        regionPref.setEntryValues(regions);
        regionPref.setDefaultValue(regions[0]);
        regionPref.setEnabled(true);

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // Go home
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
