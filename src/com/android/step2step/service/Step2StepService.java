package com.android.step2step.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

public class Step2StepService extends Service {
	public Step2StepSensor sensor = null;
	
	private final IBinder binder = new Step2StepBinder();
	
    public class Step2StepBinder extends Binder {
    	public Step2StepService getService() {
            return Step2StepService.this;
        }
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return this.binder;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		this.sensor.stop();
		return super.onUnbind(intent);
	}


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		this.initSensors();
		return super.onStartCommand(intent, flags, startId);
	}


	public void initSensors() {
		sensor = new Step2StepSensor((SensorManager) getSystemService(SENSOR_SERVICE));
	}
	public void addSensorStepListener(ISensorStepEventListener listener) {
		if (sensor != null)
			sensor.addStepListener(listener);
	}
	public void removeSensorStepListener(ISensorStepEventListener listener) {
		if (sensor != null)
			sensor.removeStepListener(listener);
	}
}
