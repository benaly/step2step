package com.android.step2step.service;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.android.step2step.misc.Step2StepMetricMode;
import com.android.step2step.misc.Step2StepMode;
import com.android.step2step.misc.Step2StepOption;
import com.android.step2step.misc.Step2StepSensorSensitivity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus.Listener;
import android.util.Log;

public class Step2StepSensor implements SensorEventListener
{	
	private static double METRIC_RUNNING_FACTOR = 1.02784823;
	private static double METRIC_WALKING_FACTOR = 0.708;
	
	private static int DELAY = 1000;
	private static int PERIOD = 10000;
	private static int SPEED_AVERAGE_TIME = 1;
	
	private  double metric_factor = 0.708;
	private double sensor_sensitivity = 1.5;
	private double steplength = 0.6;
    private double bodyWeight = 70;
    
	private ArrayList<ISensorStepEventListener> stepListeners = new ArrayList<ISensorStepEventListener>();
    
    private double calories = 0;
    private double averageSpeed = 0;
    private int steps = 0;
    private double distance = 0;

    private double speed = 0;
    
    private Date date = null;
    private Date datePause = null;
    private Date lastTime = null;
    private int  lastTimeSteps = 0;
    
    private long elapsedTimeInPause = 0;
    private String elapsedTime = "";
    
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Step2StepOption option = new Step2StepOption();
    
    public boolean isRunning = false;
    public boolean isPaused = false;
    
	public Step2StepSensor(SensorManager sensorManager)
	{
		this.mSensorManager = sensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
        if (countSteps(event) == true)
        	onSteps();
        this.notifyListener();
	}

	public void onSteps() {
		this.calcStepValue();
	}

	
	private Boolean countSteps(SensorEvent event) {
		float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        
        double g = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        if(g >= this.sensor_sensitivity)
        	return true;
        return false;
	}
	
	
	public void addStepListener(ISensorStepEventListener listener) {
		stepListeners.add(listener);
	}
	public void removeStepListener(ISensorStepEventListener listener) {
		stepListeners.remove(listener);
	}
	
	public void start(Step2StepOption option) {
	
		if (isRunning && isPaused) {
			Date currentTime = new Date();
			
			isPaused = false;
			this.elapsedTimeInPause += currentTime.getTime() - datePause.getTime();
			for (ISensorStepEventListener listener : stepListeners) {
				listener.onSenSorPause(false);
			}
		}
		if (!isRunning && !isPaused) {

			this.reset();
			isRunning = true;
			this.option = option;
			this.loadPref();
			for (ISensorStepEventListener listener : stepListeners) {
				listener.onSenSorStart();
			}
		}
		mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stop() {
		if (isRunning) {
			isRunning = false;
			isPaused = false;
			mSensorManager.unregisterListener(this);
	    	for (ISensorStepEventListener listener : stepListeners) {
	    		listener.onSenSorStop();
			}
		}
	}
	public void pause(boolean complete) {
		if (isRunning && !isPaused) {
			
			isPaused = true;
			this.datePause = new Date();
			mSensorManager.unregisterListener(this);
	    	for (ISensorStepEventListener listener : stepListeners) {
	    		listener.onSenSorPause(complete);
			}
		}
	}
	public boolean reset() {
	    
	    this.calories = 0;
	    this.averageSpeed = 0;
	    this.steps = 0;
	    this.distance = 0;
	    this.speed = 0;
	    
	    this.lastTimeSteps = 0;
	    this.elapsedTimeInPause = 0;
	    
	    this.lastTime = new Date();
	    
	    this.date = new Date();
	    this.datePause = null;
	    
    	for (ISensorStepEventListener listener : stepListeners) {
    		listener.onSenSorReset();
		}
    	return false;
	}
	
	@SuppressLint("NewApi")
	public void checkStopOnMode() {
		
		if (this.option.currentMode == Step2StepMode.DURATION) {
			Date currentTime = new Date(System.currentTimeMillis());
			long minute = TimeUnit.MILLISECONDS.toMinutes(currentTime.getTime() - date.getTime());
			if (minute >= this.option.limitTime)
				this.pause(true);
		}
		if (this.option.currentMode == Step2StepMode.DISTANCE) {
			if (this.distance >= this.option.limitDistance) {
				this.pause(true);
			}
		}
		
	}
	
	public void notifyListener() {
		
		this.checkStopOnMode();
		this.calcAverageValue();
		for (ISensorStepEventListener listener : stepListeners) {
			listener.onStep(steps);
			listener.onDistance(distance);
			listener.onDuration(elapsedTime);
			listener.onAverageSpeed(averageSpeed);
			listener.onCalorie(calories);
			listener.onSpeed(speed);
		}	
	}
	
	public void calcAverageValue() {
		Date currentTime = new Date(System.currentTimeMillis());
		long time = currentTime.getTime() - date.getTime();
		double sec = TimeUnit.MILLISECONDS.toSeconds(time);
		
		if (lastTime == null || TimeUnit.MILLISECONDS.toSeconds(currentTime.getTime() - lastTime.getTime()) >= SPEED_AVERAGE_TIME) {
			lastTime = currentTime;
			
			int diff = this.steps - this.lastTimeSteps;
			this.speed = (((diff * this.steplength) / 1000) / SPEED_AVERAGE_TIME) * 3600;
			this.lastTimeSteps = this.steps;
		}
		
    	this.averageSpeed = ((this.distance * 1000 / sec) * 3600) / 1000;
    	this.elapsedTime = formatDate();
	}
	
	public void calcStepValue() {
		this.steps += 1;
		this.distance += this.steplength / 1000;
    	this.calories += (bodyWeight * metric_factor)* steplength / 100000.0;
	}
	
	public String formatDate() {
		Date currentDate = new Date();
		return formatInterval((currentDate.getTime() - elapsedTimeInPause) - date.getTime());
	}
	
	@SuppressLint("NewApi")
	private String formatInterval(final long l)
	{
		long hr = TimeUnit.MILLISECONDS.toHours(l);
		long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
		long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d", hr, min, sec);
	}
	
	private void loadPref() {
		// TODO Auto-generated method stub
		this.metric_factor = option.metricMode == Step2StepMetricMode.WALKING ? METRIC_WALKING_FACTOR : METRIC_RUNNING_FACTOR;
		this.steplength = this.option.stepLength;
		this.bodyWeight = this.option.weigth;
		
		if (this.option.sensorSensitivity == Step2StepSensorSensitivity.LOW)
			this.sensor_sensitivity = 2;
		else if (this.option.sensorSensitivity == Step2StepSensorSensitivity.MEDIUM)
			this.sensor_sensitivity = 1.5;
		else 
			this.sensor_sensitivity = 1.1;
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
