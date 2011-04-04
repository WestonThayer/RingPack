package com.cryclops.ringpack;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * See preferences.xml
 * 
 * @author Cryclops
 * @version 1.0.0
 *
 */
public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}