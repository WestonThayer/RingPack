package com.cryclops.ringpack;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;

import com.cryclops.ringpack.utils.ServiceUtils;

/**
 *
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference goToAccessibilitySettings = (Preference) this.findPreference("goToAccessibilitySettings");
        goToAccessibilitySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServiceUtils.getLog().activityStart(this);
    }

    @Override
    protected void onStop() {
        ServiceUtils.getLog().activityStop(this);
        super.onStop();
    }
}
