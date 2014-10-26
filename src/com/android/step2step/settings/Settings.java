package com.android.step2step.settings;

import com.android.step2step.misc.Step2StepMetricMode;
import com.android.step2step.misc.Step2StepMetricSys;
import com.android.step2step.misc.Step2StepSensorSensitivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	
	private SharedPreferences sharedPref = null;
	private Context context = null;
	
	public Settings(final Context context) {
        this.context = context;
    }
	
	public Step2StepMetricSys getSysmetric()
	{
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String tmp = this.sharedPref.getString("listPrefUnit", "NULL");
		return tmp.contentEquals("KM") ? Step2StepMetricSys.KM : Step2StepMetricSys.MILES;
	}

	public Step2StepSensorSensitivity getSensitivity() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context); 
		String tmp = sharedPref.getString("listPrefSensitivity", "NULL");

		if (tmp.contentEquals("Low"))
			return Step2StepSensorSensitivity.LOW;
		else if (tmp.contentEquals("Medium"))
			return Step2StepSensorSensitivity.MEDIUM;
		return Step2StepSensorSensitivity.HIGH;
	}
	
	public Step2StepMetricMode getMetricMode() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context); 
		String tmp = sharedPref.getString("listPrefMetricMode", "NULL");
		return tmp.contentEquals("Walking") ? Step2StepMetricMode.WALKING : Step2StepMetricMode.RUNNING;
	}
	
	public double getStepLength()
	{
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return Double.valueOf(this.sharedPref.getString("prefStepLength", "NULL"));
	}
	
	public double getWeight()
	{
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return Double.valueOf(this.sharedPref.getString("prefWeight", "NULL"));
	}
}
