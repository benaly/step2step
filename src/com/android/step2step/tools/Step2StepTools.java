package com.android.step2step.tools;

import com.android.step2step.misc.Step2StepMetricSys;

public class Step2StepTools {
	
	public static double Convert(Step2StepMetricSys m, double value) {
		return m == Step2StepMetricSys.MILES ? value * 0.62137 : value;
	}
	public static String getUnity(Step2StepMetricSys m) {
		return m == Step2StepMetricSys.MILES ? "Miles"  : "Km";
	}
}
