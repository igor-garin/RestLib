package com.foxykeep.datadroidpoc.ui;

import ru.igarin.base.service.SingleRequestManagerActivityHelper;
import ru.igarin.base.service.SingleRequestManagerActivityHelper.RequestListener;

import com.foxykeep.datadroid.config.ThisService;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private SingleRequestManagerActivityHelper<ThisService> mRequestHelper = null;
	private RequestListener mRequestListener = new RequestListener(){

		@Override
		public void onRequestFinished(int requestType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestStarted(int requestType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestDataError(int requestType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestConnectionError(int requestType, int statusCode) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestCustomError(int requestType, Bundle resultData) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestSuccessed(int requestType, Bundle payload) {
			// TODO Auto-generated method stub

		}
		
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRequestHelper = new SingleRequestManagerActivityHelper<ThisService>(
				ThisService.class, this, savedInstanceState, mRequestListener);
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

}
