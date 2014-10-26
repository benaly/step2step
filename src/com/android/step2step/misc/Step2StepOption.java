package com.android.step2step.misc;


public class Step2StepOption {

	public Step2StepMode currentMode = Step2StepMode.FREE;	
	public Step2StepSensorSensitivity sensorSensitivity = Step2StepSensorSensitivity.LOW;
	public Step2StepMetricSys sysMetric = Step2StepMetricSys.KM;
	public Step2StepMetricMode metricMode = Step2StepMetricMode.WALKING;
	
	public double stepLength = 0;
	public double weigth = 0;
	
	public int limitDistance = 0;
	public int limitTime = 0;
}
