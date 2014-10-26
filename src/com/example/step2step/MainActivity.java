package com.example.step2step;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.step2step.misc.Step2StepMode;
import com.android.step2step.misc.Step2StepOption;
import com.android.step2step.service.ISensorStepEventListener;
import com.android.step2step.service.IServiceNotifier;
import com.android.step2step.service.Step2StepService;
import com.android.step2step.service.Step2StepServiceCon;
import com.android.step2step.settings.*;
import com.android.step2step.tools.Step2StepTools;

public class MainActivity extends Activity implements IServiceNotifier, ISensorStepEventListener
{
	private Settings	appSettings = null;
	private Step2StepServiceCon conn = null;
	private Step2StepService service = null;
	private Step2StepOption option = null;
	
	private ViewFlipper _flipper;
	private LinearLayout _activateButton = null;
	
	private TextView _principal = null;
	private TextView _second = null;
	private TextView _goal = null;
	private TextView _goalUnit = null;
	private TextView _altitudeCount = null;
	private TextView _cityLocation = null;
			
	private ImageButton _start = null;
	private ImageButton _stop = null;
	private ImageButton _pause = null;
	private ImageButton _padLock = null;
	private ImageButton _distanceIcon = null;
	private ImageButton _durationIcon = null;
	private ImageButton _freeIcon = null;
	
	private boolean _checkActivity = true;
	private boolean _checkPause = true;
	private boolean _checkPadLock = true;
	private boolean _isTraining = false;
	private boolean _checkMode = false;
	
	private static final int _SWIPE_MIN_DISTANCE = 120;
    private static final int _SWIPE_THRESHOLD_VELOCITY = 200;
    final private static int _DIALOG_DURATION = 1;
    final private static int _DIALOG_DISTANCE = 2;
    
    private String _hours_string = null;
	private String _minutes_string = null;
    private String _distance_edit = null;
    
    private Typeface _type = null; 
    private Typeface _typeTitle = null;
    
    private List<String> _accessibleProviders = null;
    
    private LocationManager _locationManager = null;
    private LocationListener _locationListener = new MyLocationListener();  
    
	private View.OnClickListener clickPrincipal = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			if (_checkActivity == false)
        	{
				_flipper.setInAnimation(inFromLeftAnimation());
				_flipper.setOutAnimation(outToRightAnimation());
				_flipper.showPrevious();
				_checkActivity = true;
        	}
		}
	};
	
	private View.OnClickListener clickSecond = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			if (_checkActivity == true)
        	{
				_flipper.setInAnimation(inFromRightAnimation());
				_flipper.setOutAnimation(outToLeftAnimation());
				_flipper.showNext(); 
				_checkActivity = false;
        	}
		}
	};
	
	private View.OnClickListener clickStart = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (_checkMode == true)
			{
				if (_isTraining == false)
				{
					if (_accessibleProviders != null && _accessibleProviders.size() > 0)
						_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 1, _locationListener);
					_start.setVisibility(View.GONE);
					_activateButton.setVisibility(View.VISIBLE);
					_isTraining = true;
					_durationIcon.setBackgroundResource(R.drawable.duration_icon_disable);
					_durationIcon.setClickable(false);
					_distanceIcon.setBackgroundResource(R.drawable.distance_icon_disable);
					_distanceIcon.setClickable(false);
					_freeIcon.setBackgroundResource(R.drawable.free_run_disable);
					_freeIcon.setClickable(false);
				
					Step2StepOption option = MainActivity.this.option;
					
					if (option.currentMode == Step2StepMode.DURATION) 
					{
						option.limitTime = Integer.parseInt(_minutes_string);
						option.limitTime += 60 * Integer.parseInt(_hours_string);
						Log.d(STORAGE_SERVICE,"CHEVAL --> " + MainActivity.this.option.limitTime);
					}
					else if (option.currentMode == Step2StepMode.DISTANCE) 
					{
						option.limitDistance = Integer.parseInt(_goal.getText().toString());
					}
					else if (option.currentMode == Step2StepMode.FREE)
					{
						option.limitDistance = option.limitTime = 0;
					}
					
					//BEURK -->
					MainActivity.this.getOptionFromPref();
					service.sensor.start(MainActivity.this.option);
				}
			}
			else
				Toast.makeText(getApplicationContext(), "Please select mode", Toast.LENGTH_SHORT).show();
		}
	};
	
	private void getOptionFromPref() {
		// TODO Auto-generated method stub
		this.option.sensorSensitivity = this.appSettings.getSensitivity();
		this.option.sysMetric = this.appSettings.getSysmetric();
		this.option.metricMode = this.appSettings.getMetricMode();
		
		this.option.stepLength = this.appSettings.getStepLength() > 0 ? this.appSettings.getStepLength() : 1.5;
		this.option.weigth = this.appSettings.getWeight();
	}
	
	private View.OnClickListener clickStop = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (_isTraining == true)
			{
				if (_accessibleProviders != null && _accessibleProviders.size() > 0)
					_locationManager.removeUpdates(_locationListener);
				_activateButton.setVisibility(View.GONE);
				_start.setVisibility(View.VISIBLE);
				_padLock.setBackgroundResource(R.drawable.padlock_lock);
				_pause.setBackgroundResource(R.drawable.pause_disable);
				_pause.setClickable(false);
				_stop.setBackgroundResource(R.drawable.stop_disable);
				_stop.setClickable(false);
				_durationIcon.setBackgroundResource(R.drawable.duration_icon);
				_durationIcon.setClickable(true);
				_distanceIcon.setBackgroundResource(R.drawable.distance_icon);
				_distanceIcon.setClickable(true);
				_freeIcon.setBackgroundResource(R.drawable.free_run);
				_freeIcon.setClickable(true);
				_goal.setText("");
				_goal.setVisibility(View.GONE);
				_goalUnit.setVisibility(View.GONE);
				_checkPadLock = true;
				_isTraining = false;
				_checkMode = false;
				service.sensor.stop();
			}
		}
	};
	
	private View.OnClickListener clickPause = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (_isTraining == true)
			{
				if (_checkPause == true)
				{
					_pause.setBackgroundResource(R.drawable.continu);
					_checkPause = false;
					service.sensor.pause(false);
				}
				else
				{
					_pause.setBackgroundResource(R.drawable.pause);
					_checkPause = true;
					service.sensor.start(MainActivity.this.option);
				}
			}
		}
	};
	
	private View.OnClickListener clickPadLock = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (_checkPadLock == true)
			{
				if (_checkPause == false)
					_pause.setBackgroundResource(R.drawable.continu);
				else
					_pause.setBackgroundResource(R.drawable.pause);
				_padLock.setBackgroundResource(R.drawable.padlock_open);
				_pause.setClickable(true);
				_stop.setBackgroundResource(R.drawable.stop);
				_stop.setClickable(true);
				_checkPadLock = false;
			}
			else
			{
				if (_checkPause == false)
					_pause.setBackgroundResource(R.drawable.continu_disable);
				else
					_pause.setBackgroundResource(R.drawable.pause_disable);
				_padLock.setBackgroundResource(R.drawable.padlock_lock);
				_pause.setClickable(false);
				_stop.setBackgroundResource(R.drawable.stop_disable);
				_stop.setClickable(false);
				_checkPadLock = true;
			}
		}
	};
	
	private	void pauseState()
	{
		_checkPause = false;
		if (_checkPadLock == true)
		{
			_stop.setBackgroundResource(R.drawable.stop_disable);
			_pause.setBackgroundResource(R.drawable.continu_disable);
		}
		else
		{
			_stop.setBackgroundResource(R.drawable.stop);
			_pause.setBackgroundResource(R.drawable.continu);
		}
	}
	
	private View.OnClickListener clickDuration = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			showDialog(_DIALOG_DURATION);
			MainActivity.this.option.currentMode =  Step2StepMode.DURATION;
			_durationIcon.setBackgroundResource(R.drawable.duration_icon_press);
			_distanceIcon.setBackgroundResource(R.drawable.distance_icon);
			_freeIcon.setBackgroundResource(R.drawable.free_run);
			_checkMode = true;
		}
	};
	
	private View.OnClickListener clickDistance = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			showDialog(_DIALOG_DISTANCE);
			MainActivity.this.option.currentMode =  Step2StepMode.DISTANCE;
			_distanceIcon.setBackgroundResource(R.drawable.distance_icon_press);
			_durationIcon.setBackgroundResource(R.drawable.duration_icon);
			_freeIcon.setBackgroundResource(R.drawable.free_run);
			_checkMode = true;
		}
	};
	
	private View.OnClickListener clickFree = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			MainActivity.this.option.currentMode =  Step2StepMode.FREE;
			_freeIcon.setBackgroundResource(R.drawable.free_run_press);
			_durationIcon.setBackgroundResource(R.drawable.duration_icon);
			_distanceIcon.setBackgroundResource(R.drawable.distance_icon);
			_goal.setText("∞");
			_goal.setVisibility(View.VISIBLE);
			_goalUnit.setVisibility(View.GONE);
			_checkMode = true;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		_type = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/opificio.ttf");
		_typeTitle = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/dod2.ttf");
		actionCustom();
		initialize();
		setFont();
		
		_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		_accessibleProviders = _locationManager.getProviders(true);
		
		
		
	}
	
	private void initialize()
	{
		final GestureDetector gdt = new GestureDetector(new GestureListener());
		
		
		_flipper = (ViewFlipper) findViewById(R.id.flipper);
		_flipper.setOnTouchListener(new OnTouchListener() 
		{
			@Override
			public boolean onTouch(final View view, final MotionEvent event) 
			{
				gdt.onTouchEvent(event);
				return true;
			}
		});

		_principal = (TextView) findViewById(R.id.principal_text_sec);
		_principal.setOnClickListener(clickPrincipal);
		_principal.setTypeface(_type);
		
		_second = (TextView) findViewById(R.id.second_text);
		_second.setOnClickListener(clickSecond);
		_second.setTypeface(_type);

		_goal = (TextView) findViewById(R.id.goal_count);
		_goal.setVisibility(View.GONE);
		_goal.setTypeface(_type);
		
		_goalUnit = (TextView) findViewById(R.id.goal_unit);
		_goalUnit.setVisibility(View.GONE);
		_goalUnit.setTypeface(_type);
		
		_start = (ImageButton) findViewById(R.id.start_training);
		_start.setOnClickListener(clickStart);

		_pause = (ImageButton) findViewById(R.id.pause_training);
		_pause.setOnClickListener(clickPause);
		_pause.setClickable(false);
		
		_stop = (ImageButton) findViewById(R.id.stop_training);
		_stop.setOnClickListener(clickStop);
		_stop.setClickable(false);
		
		_padLock = (ImageButton) findViewById(R.id.padlock);
		_padLock.setOnClickListener(clickPadLock);
		
		_distanceIcon = (ImageButton) findViewById(R.id.distance_icon);
		_distanceIcon.setOnClickListener(clickDistance);
		
		_durationIcon = (ImageButton) findViewById(R.id.duration_icon);
		_durationIcon.setOnClickListener(clickDuration);
		
		_freeIcon = (ImageButton) findViewById(R.id.free_icon);
		_freeIcon.setOnClickListener(clickFree);

		_activateButton = (LinearLayout) findViewById(R.id.activate_button);
		
		this.option = new Step2StepOption();
		this.appSettings = new Settings(getApplicationContext());
		this.conn = new Step2StepServiceCon(this, new Intent(MainActivity.this, Step2StepService.class), this);
		this.conn.startService();
		 
		
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		this.cleanGUI();
	}
	
	private void setFont()
	{
		TextView principalFirst = (TextView) findViewById(R.id.principal_text);
		principalFirst.setTypeface(_type);
		
		TextView secondText = (TextView) findViewById(R.id.second_text_sec);
		secondText.setTypeface(_type);
		
		TextView durationText = (TextView) findViewById(R.id.duration_text);
		durationText.setTypeface(_type);
		TextView distanceText = (TextView) findViewById(R.id.distance_text);
		distanceText.setTypeface(_type);
		TextView distanceUnit = (TextView) findViewById(R.id.distance_unit);
		distanceUnit.setTypeface(_type);
		
		TextView goalText = (TextView) findViewById(R.id.goal_text);
		goalText.setTypeface(_type);
		
		TextView speedUnit = (TextView) findViewById(R.id.speed_unit);
		speedUnit.setTypeface(_type);
		TextView speedText = (TextView) findViewById(R.id.speed_text);
		speedText.setTypeface(_type);
		
		TextView altitudeUnit = (TextView) findViewById(R.id.altitude_unit);
		altitudeUnit.setTypeface(_type);
		TextView altitudeText = (TextView) findViewById(R.id.altitude_text);
		altitudeText.setTypeface(_type);
		_altitudeCount = (TextView) findViewById(R.id.altitude_count);
		_altitudeCount.setTypeface(_type);
		
		TextView averageUnit = (TextView) findViewById(R.id.average_speed_unit);
		averageUnit.setTypeface(_type);
		TextView averageText = (TextView) findViewById(R.id.average_speed_text);
		averageText.setTypeface(_type);
		
		TextView calorieText = (TextView) findViewById(R.id.calorie_text);
		calorieText.setTypeface(_type);
		
		_cityLocation = (TextView) findViewById(R.id.city_location);
		_cityLocation.setTypeface(_type);
	}
	
	private void actionCustom()
	{
		ActionBar myActionBar = getActionBar();
		myActionBar.setDisplayShowHomeEnabled(false);
		
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);
		 
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.titleview, null);
		
		((TextView)v.findViewById(R.id.title_1)).setText("STEP");
		((TextView)v.findViewById(R.id.title_1)).setTypeface(_typeTitle);
		((TextView)v.findViewById(R.id.title_2)).setText("2");
		((TextView)v.findViewById(R.id.title_2)).setTypeface(_typeTitle);
		((TextView)v.findViewById(R.id.title_3)).setText("STEP");
		((TextView)v.findViewById(R.id.title_3)).setTypeface(_typeTitle);
		
		myActionBar.setCustomView(v);
	}
	
    public void cleanGUI() 
    {
		TextView distance = (TextView)findViewById(R.id.distance_count);
		distance.setText("--");
		
		TextView duration = (TextView)findViewById(R.id.duration_count);
		duration.setText("--:--:--");
		
		TextView calories = (TextView)findViewById(R.id.calorie_count);
		calories.setText("-");
		
		TextView speed = (TextView)findViewById(R.id.speed_count);
		speed.setText("-");
		
		TextView aspeed = (TextView)findViewById(R.id.average_speed);
		aspeed.setText("-");
		
		TextView alt = (TextView)findViewById(R.id.altitude_count);
		alt.setText("-");
    }
	
    public void getAltitude()
    {
    	
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//this.conn.stopService();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.conn.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.conn.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	private Animation inFromRightAnimation()
	{
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		inFromRight.setDuration(300);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}
	private Animation outToLeftAnimation() 
	{
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		outtoLeft.setDuration(300);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}
	
	private Animation inFromLeftAnimation() 
	{
		Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		inFromLeft.setDuration(300);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}
	private Animation outToRightAnimation() 
	{
		Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		outtoRight.setDuration(300);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
	
	@Override
	 protected Dialog onCreateDialog(int id) 
	{
	  AlertDialog dialogDetails = null;
	  
	  switch (id) 
	  {
	  case _DIALOG_DURATION:
		  LayoutInflater inflater = LayoutInflater.from(this);
		  View dialogview = inflater.inflate(R.layout.dialog_duration_layout, null);

		  AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
		  dialogbuilder.setView(dialogview);
		  dialogDetails = dialogbuilder.create();

		  break;
	  case _DIALOG_DISTANCE:
		  LayoutInflater inflater_distance = LayoutInflater.from(this);
		  View dialogview_distance = inflater_distance.inflate(R.layout.dialog_distance_layout, null);
		  
		  AlertDialog.Builder dialogbuilder_distance = new AlertDialog.Builder(this);
		  dialogbuilder_distance.setView(dialogview_distance);
		  dialogDetails = dialogbuilder_distance.create();
		  break;
	  }
	  return dialogDetails;
	}
	
	@Override
	 protected void onPrepareDialog(int id, Dialog dialog) 
	{			
		switch (id) 
		{
		case _DIALOG_DURATION:
			final AlertDialog alertDialog = (AlertDialog) dialog;
			
			Button okButton = (Button) alertDialog.findViewById(R.id.btn_login);
			Button cancelButton = (Button) alertDialog.findViewById(R.id.btn_cancel);
			
			TextView _dialogDuration = (TextView) alertDialog.findViewById(R.id.enter_duration);
			_dialogDuration.setTypeface(_type);
			
			final EditText hours = (EditText) alertDialog.findViewById(R.id.duration_hour);
			final EditText minutes = (EditText) alertDialog.findViewById(R.id.duration_minute);

			hours.setText("");
			minutes.setText("");

			alertDialog.setCancelable(false);
			okButton.setOnClickListener(new View.OnClickListener() 
			{

				@Override
				public void onClick(View v) 
				{
					_hours_string = hours.getText().toString();
					_minutes_string = minutes.getText().toString();

					if ((!_hours_string.equals("") && Integer.parseInt(_hours_string) > 0 && Integer.parseInt(_hours_string) <= 23) || (!_minutes_string.equals("") && Integer.parseInt(_minutes_string) > 0 && Integer.parseInt(_minutes_string) <= 59))
					{
						if (_hours_string.equals(""))
							_hours_string = "00";
						if (_minutes_string.equals(""))
							_minutes_string = "00";
						if (_hours_string.length() == 1)
							_hours_string = "0" + _hours_string;
						if (_minutes_string.length() == 1)
							_minutes_string = "0" + _minutes_string;
						_goal.setText(_hours_string + ":" + _minutes_string);
						_goal.setVisibility(View.VISIBLE);
						_goalUnit.setVisibility(View.GONE);
						alertDialog.dismiss();
					}
					else
					{
						if (Integer.parseInt(_hours_string) > 23)
							Toast.makeText(getApplicationContext(), "Hours must be less than 24", Toast.LENGTH_SHORT).show();
						else if (Integer.parseInt(_minutes_string) > 59)
							Toast.makeText(getApplicationContext(), "Minutes must be less than 59", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(getApplicationContext(), "Fill the fields", Toast.LENGTH_SHORT).show();
					}
				}});
			cancelButton.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					_goal.setText("");
					_goal.setVisibility(View.GONE);
					_goalUnit.setVisibility(View.GONE);
					_durationIcon.setBackgroundResource(R.drawable.duration_icon);
					_checkMode = false;
					alertDialog.dismiss();
				}});
			break;
		case _DIALOG_DISTANCE:			
			final AlertDialog alertDialog_distance = (AlertDialog) dialog;
			
			Button okButton_distance = (Button) alertDialog_distance.findViewById(R.id.btn_login_dist);
			Button cancelButton_distance = (Button) alertDialog_distance.findViewById(R.id.btn_cancel_dist);
			
			TextView _dialogDistance = (TextView) alertDialog_distance.findViewById(R.id.enter_distance);
			_dialogDistance.setTypeface(_type);
			
			final EditText edit_dist = (EditText) alertDialog_distance.findViewById(R.id.distance_edit);

			edit_dist.setText("");
			alertDialog_distance.setCancelable(false);
			okButton_distance.setOnClickListener(new View.OnClickListener() 
			{

				@Override
				public void onClick(View v) 
				{
					_distance_edit = edit_dist.getText().toString();

					if ((!_distance_edit.equals("") && Integer.parseInt(_distance_edit) > 0 && Integer.parseInt(_distance_edit) <= 99))
					{
						_goal.setText(_distance_edit);
						_goal.setVisibility(View.VISIBLE);
						_goalUnit.setVisibility(View.VISIBLE);
						alertDialog_distance.dismiss();
					}
					else
						Toast.makeText(getApplicationContext(), "Fill the field", Toast.LENGTH_SHORT).show();
				}});
			cancelButton_distance.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					_goal.setText("");
					_goal.setVisibility(View.GONE);
					_goalUnit.setVisibility(View.GONE);
					_distanceIcon.setBackgroundResource(R.drawable.distance_icon);
					_checkMode = false;
					alertDialog_distance.dismiss();
				}});
			
			break;

		}
	}
	
    private class GestureListener extends SimpleOnGestureListener 
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
        {
            if(e1.getX() - e2.getX() > _SWIPE_MIN_DISTANCE && Math.abs(velocityX) > _SWIPE_THRESHOLD_VELOCITY) 
            {
            	if (_checkActivity == true)
            	{
            		_flipper.setInAnimation(inFromRightAnimation());
            		_flipper.setOutAnimation(outToLeftAnimation());
            		_flipper.showNext();
            		_checkActivity = false;
            	}
                return false; // Right to left
            }  
            else if (e2.getX() - e1.getX() > _SWIPE_MIN_DISTANCE && Math.abs(velocityX) > _SWIPE_THRESHOLD_VELOCITY) 
            {
            	if (_checkActivity == false)
            	{
            		_flipper.setInAnimation(inFromLeftAnimation());
            		_flipper.setOutAnimation(outToRightAnimation());
            		_flipper.showPrevious();
            		_checkActivity = true;
            	}
    	        
                return false; // Left to right
            }
            return false;
        }
    }

	@Override
	public void onSenSorStart() 
	{
		// TODO Auto-generated method stub
		this.cleanGUI();
	}

	@Override
	public void onSenSorStop() 
	{
		// TODO Auto-generated method stub
		this.cleanGUI();
	}

	@Override
	public void onSenSorPause(boolean complete) 
	{
		// TODO Auto-generated method stub
		if (complete)
			this.pauseState();
	}

	@Override
	public void onSenSorReset() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStep(int steps) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDistance(double distance) 
	{
		// TODO Auto-generated method stub
		TextView view = (TextView)findViewById(R.id.distance_count);
		view.setTypeface(_type);
		view.setText(String.format("%.3f", Step2StepTools.Convert(option.sysMetric, distance)));
	}

	@Override
	public void onDuration(String string) 
	{
		// TODO Auto-generated method stub
		TextView view = (TextView)findViewById(R.id.duration_count);
		view.setTypeface(_type);
		view.setText(string);
	}


	@Override
	public void onCalorie(double calories) 
	{
		// TODO Auto-generated method stub
		TextView view = (TextView)findViewById(R.id.calorie_count);
		view.setTypeface(_type);
		view.setText(String.format("%.2f", calories));
	}


	@Override
	public void onSpeed(double speed) 
	{
		// TODO Auto-generated method stub
		TextView view = (TextView)findViewById(R.id.speed_count);
		view.setTypeface(_type);
		view.setText(String.format("%.2f", Step2StepTools.Convert(option.sysMetric, speed)));
	}


	@Override
	public void onAverageSpeed(double averageSpeed) 
	{
		// TODO Auto-generated method stub
		TextView view = (TextView)findViewById(R.id.average_speed);
		view.setTypeface(_type);
		view.setText(String.format("%.1f", Step2StepTools.Convert(option.sysMetric, averageSpeed)));
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder arg1) 
	{
		// TODO Auto-generated method stub
		if (service == null) {
			service = ((Step2StepService.Step2StepBinder)arg1).getService();
			service.addSensorStepListener(this);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) 
	{
		// TODO Auto-generated method stub
		service.removeSensorStepListener(this);
		service = null;
	}
	
	
private class MyLocationListener implements LocationListener 
	{

		@Override
		public void onLocationChanged(Location loc) 
		{
			String altitude = "" + loc.getAltitude();
			_altitudeCount.setText(altitude.substring(0, 2));
			
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingActivity.class);
			this.startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
