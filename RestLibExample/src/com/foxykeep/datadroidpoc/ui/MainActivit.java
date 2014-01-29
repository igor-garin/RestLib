package com.foxykeep.datadroidpoc.ui;

import com.foxykeep.datadroid.config.ThisService;
import com.foxykeep.datadroid.workers.Worker;

import ru.igarin.base.restlib.service.RequestManager;
import ru.igarin.base.restlib.service.SimpleReqParams;
import ru.igarin.base.restlib.service.SingleRequestManagerActivityHelper;
import ru.igarin.base.restlib.service.RequestManager.OnRequestFinishedListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class MainActivit extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = this;

		RequestManager mRequestManager = RequestManager.from(ThisService.class,
				context);
		mRequestManager
				.addOnRequestFinishedListener(new OnRequestFinishedListener() {

					@Override
					public void onRequestFinished(int requestId,
							Bundle resultData) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestConnectionError(int requestId,
							int statusCode) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestDataError(int requestId) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestCustomError(int requestId,
							Bundle resultData) {
						// TODO Auto-generated method stub

					}

				});
		
		mRequestManager.startRequestForWorker(Worker.class, new SimpleReqParams());
	}

}
