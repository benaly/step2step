package com.android.step2step.service;

import android.content.ComponentName;
import android.os.IBinder;

public interface IServiceNotifier {
	public void onServiceConnected(ComponentName name, IBinder service);
	public void onServiceDisconnected(ComponentName name);
}
