package com.example.ru.mera.restlibexample;

import com.example.ru.mera.restlibexample.rest.ThisService;

import ru.igarin.base.restlib.service.SingleRequestManagerActivityHelper;
import ru.igarin.base.restlib.service.SingleRequestManagerActivityHelper.RequestListener;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity implements RequestListener {
	
	private SingleRequestManagerActivityHelper<ThisService> mRequestHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRequestHelper = new SingleRequestManagerActivityHelper<ThisService>(
				ThisService.class, this, savedInstanceState, this);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		mRequestHelper.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mRequestHelper.onPause();
	}

	
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		mRequestHelper.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onConnexionError(int arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHandleError(int arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestFinished(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestStarted(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestSuccessed(int arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
