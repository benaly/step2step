package com.android.step2step.service;

public interface ISensorStepEventListener {
	
	public void onSenSorStart();
	public void onSenSorStop();
	public void onSenSorPause(boolean complete);
	public void onSenSorReset();
	
	public void onStep(int steps);
	
	public void onDistance(double distance);
	
	public void onDuration(String string);
	
	public void onCalorie(double calories);
	
	public void onSpeed(double speed);
	
	public void onAverageSpeed(double averageSpeed);
	
}
