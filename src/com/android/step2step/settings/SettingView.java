package com.android.step2step.settings;


import com.example.step2step.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingView extends PreferenceFragment {
	
	public static final String TAG = "MyPreferenceFragment";
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.settings);
    }
}
