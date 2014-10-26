package com.android.step2step.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class Step2StepServiceCon implements ServiceConnection {
	private IServiceNotifier notifier = null;
	private Intent service = null;
	private Activity activity = null;
	
	private boolean isBinded = false;
	private boolean isStarted = false;
	
	public Step2StepServiceCon(Activity activity, Intent service, IServiceNotifier notifier) {
		this.notifier = notifier;
		this.service = service;
		this.activity = activity;
	}
	
	public boolean tryToBind() {
		return this.bindService();
	}
	
	public boolean startService() {
		isStarted = activity.startService(service) != null;
		isBinded = this.bindService();
		return isStarted && isBinded;
	}
	
	public boolean onPause() {
		return isStarted ? this.unbindService() : false;
	}
	
	public boolean onResume() {
		return isStarted ? this.bindService() : false;
	}
	
	public boolean stopService() {
		
		isBinded = this.unbindService();
		isStarted = activity.stopService(service);
		return isStarted && isBinded;
	}
	
	private boolean bindService() {
		return activity.bindService(service, this, activity.BIND_AUTO_CREATE);
	}

	private boolean unbindService() {
		activity.unbindService(this);
		return true;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		if (this.notifier != null)
			notifier.onServiceConnected(name, service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		if (this.notifier != null)
			notifier.onServiceDisconnected(name);
	}
}
