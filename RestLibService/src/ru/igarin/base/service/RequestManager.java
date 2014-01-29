package ru.igarin.base.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.SparseArray;

/**
 * This class is used as a proxy to call the Service. It provides easy-to-use
 * methods to call the service and manages the Intent creation. It also assures
 * that a request will not be sent again if an exactly identical one is already
 * in progress
 * 
 */
public class RequestManager {

	public static final String INTENT_EXTRA_ERROR_STATUS = "INTENT_EXTRA_ERROR_STATUS";

	static final String RECEIVER_EXTRA_REQUEST_ID = "com.foxykeep.datadroid.extras.requestId";
	static final String RECEIVER_EXTRA_RESULT_CODE = "com.foxykeep.datadroid.extras.code";
	static final String RECEIVER_EXTRA_PAYLOAD = "com.foxykeep.datadroid.extras.payload";
    public static final String RECEIVER_EXTRA_CONNECTION_ERROR_STATUS_CODE =
            "com.foxykeep.datadroid.extra.connectionErrorStatusCode";
	static final String RECEIVER_EXTRA_ERROR_TYPE = "com.foxykeep.datadroid.extras.error";
	static final int RECEIVER_EXTRA_VALUE_ERROR_TYPE_CONNEXION = 1;
	static final int RECEIVER_EXTRA_VALUE_ERROR_TYPE_DATA = 2;
    static final int RECEIVER_EXTRA_VALUE_ERROR_TYPE_CUSTOM = 3;

	static final int MAX_RANDOM_REQUEST_ID = 1000000;

	// Singleton management
	private volatile static RequestManager sInstance;

	public synchronized static RequestManager from(
			Class<? extends PoCService> type, final Context context) {
		if (sInstance == null) {
			sInstance = new RequestManager(type, context);
		}
		return sInstance;
	}

	private final Class<? extends PoCService> mServiceClass;

	static Random sRandom = new Random();

	SparseArray<Intent> mRequestSparseArray;
	Context mContext;
	ArrayList<WeakReference<OnRequestFinishedListener>> mListenerList;
	Handler mHandler = new Handler();
	EvalReceiver mEvalReceiver = new EvalReceiver(mHandler);

	private RequestManager(Class<? extends PoCService> serviceClass,
			final Context context) {
		this.mServiceClass = serviceClass;
		mContext = context.getApplicationContext();
		mRequestSparseArray = new SparseArray<Intent>();
		mListenerList = new ArrayList<WeakReference<OnRequestFinishedListener>>();
	}

	/**
	 * The ResultReceiver that will receive the result from the Service
	 */
	private class EvalReceiver extends ResultReceiver {
		EvalReceiver(final Handler h) {
			super(h);
		}

		@Override
		public void onReceiveResult(final int resultCode,
				final Bundle resultData) {
			handleResult(resultCode, resultData);
		}
	}

	/**
	 * Clients may implements this interface to be notified when a request is
	 * finished
	 * 
	 */
	public static interface OnRequestFinishedListener extends EventListener {

        /**
         * Event fired when a request is finished.
         *
         * @param requestId The {@link Integer} defining the request.
         * @param resultData The result of the service execution.
         */
        public void onRequestFinished(int requestId, Bundle resultData);

        /**
         * Event fired when a request encountered a connection error.
         *
         * @param requestId The {@link Integer} defining the request.
         * @param statusCode The HTTP status code returned by the server (if the request succeeded
         *            by the HTTP status code was not {@link org.apache.http.HttpStatus#SC_OK}) or -1 if it was a
         *            connection problem
         */
        public void onRequestConnectionError(int requestId, int statusCode);

        /**
         * Event fired when a request encountered a data error.
         *
         * @param requestId The {@link Integer} defining the request.
         */
        public void onRequestDataError(int requestId);

        /**
         * Event fired when a request encountered a custom error.
         *
         * @param requestId The {@link Integer} defining the request.
         * @param resultData The result of the service execution.
         */
        public void onRequestCustomError(int requestId, Bundle resultData);

	}

	/**
	 * Add a {@link OnRequestFinishedListener} to this {@link RequestManager}.
	 * Clients may use it in order to listen to events fired when a request is
	 * finished.
	 * <p>
	 * <b>Warning !! </b> If it's an {@link Activity} that is used as a
	 * Listener, it must be detached when {@link Activity#onPause} is called in
	 * an {@link Activity}.
	 * </p>
	 * 
	 * @param listener
	 *            The listener to add to this {@link RequestManager} .
	 */
	public void addOnRequestFinishedListener(
			final OnRequestFinishedListener listener) {
		synchronized (mListenerList) {
			// Check if the listener is not already in the list
			if (!mListenerList.isEmpty()) {
				for (WeakReference<OnRequestFinishedListener> weakRef : mListenerList) {
					if (weakRef.get() != null && weakRef.get().equals(listener)) {
						return;
					}
				}
			}

			mListenerList.add(new WeakReference<OnRequestFinishedListener>(
					listener));
		}
	}

	/**
	 * Remove a {@link OnRequestFinishedListener} to this {@link RequestManager}
	 * .
	 * 
	 * @param listener The
	 *            listener to remove to this {@link RequestManager}.
	 */
	public void removeOnRequestFinishedListener(
			final OnRequestFinishedListener listener) {
		synchronized (mListenerList) {
			final int listenerListSize = mListenerList.size();
			for (int i = 0; i < listenerListSize; i++) {
				try {
					if (mListenerList.get(i).get().equals(listener)) {
						mListenerList.remove(i);
						return;
					}
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * Return whether a request (specified by its id) is still in progress or
	 * not
	 * 
	 * @param requestId
	 *            The request id
	 * @return whether the request is still in progress or not.
	 */
	public boolean isRequestInProgress(final int requestId) {
		return (mRequestSparseArray.indexOfKey(requestId) >= 0);
	}

	/**
	 * This method is call whenever a request is finished. Call all the
	 * available listeners to let them know about the finished request
	 * 
	 * @param resultCode
	 *            The result code of the request
	 * @param resultData
	 *            The bundle sent back by the service
	 */
	void handleResult(final int resultCode, final Bundle resultData) {

		// Get the request Id
		final int requestId = resultData.getInt(RECEIVER_EXTRA_REQUEST_ID);

		if (resultCode == PoCService.SUCCESS_CODE) {
			// TODO:
			// final Intent intent = mRequestSparseArray.get(requestId);
			// switch
			// (intent.getStringExtra(PoCService.INTENT_EXTRA_WORKER_TYPE)) {
			// }
		}

		// Remove the request Id from the "in progress" request list
		mRequestSparseArray.remove(requestId);

		// Call the available listeners
		synchronized (mListenerList) {
			for (int i = 0; i < mListenerList.size(); i++) {
				final WeakReference<OnRequestFinishedListener> weakRef = mListenerList
						.get(i);
				final OnRequestFinishedListener listener = weakRef.get();
				if (listener != null) {
					if (resultCode == PoCService.ERROR_CODE) {
                        switch (resultData.getInt(RECEIVER_EXTRA_ERROR_TYPE)) {
                            case RECEIVER_EXTRA_VALUE_ERROR_TYPE_DATA:
                                listener.onRequestDataError(requestId);
                                break;
                            case RECEIVER_EXTRA_VALUE_ERROR_TYPE_CONNEXION:
                                int statusCode =
                                        resultData.getInt(RECEIVER_EXTRA_CONNECTION_ERROR_STATUS_CODE);
                                listener.onRequestConnectionError(requestId, statusCode);
                                break;
                            case RECEIVER_EXTRA_VALUE_ERROR_TYPE_CUSTOM:
                                listener.onRequestCustomError(requestId, resultData);
                                break;
                        }
					} else {
						listener.onRequestFinished(requestId,
								resultData);
					}

				} else {
					mListenerList.remove(i);
					i--;
				}
			}
		}
	}

	public int startRequestForWorker(
			final Class<? extends IServiceWorker> workerType,
			final Parcelable params) {

		if (!IServiceWorker.class.isAssignableFrom(workerType)) {
			throw new IllegalArgumentException(
					"Class does not imlements IServiceWorker interface!");
		}

		String workerTypeName = workerType.getName();

		// Check if a match to this request is already launched
		final int requestSparseArrayLength = mRequestSparseArray.size();
		for (int i = 0; i < requestSparseArrayLength; i++) {
			final Intent savedIntent = mRequestSparseArray.valueAt(i);

			if (!savedIntent
					.getStringExtra(PoCService.INTENT_EXTRA_WORKER_TYPE)
					.equals(workerTypeName)) {
				continue;
			}
			if (!savedIntent.getParcelableExtra(
					PoCService.INTENT_EXTRA_REQUEST_PARAMS).equals(params)) {
				continue;
			}
			return mRequestSparseArray.keyAt(i);
		}

		final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

		final Intent intent = new Intent(mContext, this.mServiceClass);
		intent.putExtra(PoCService.INTENT_EXTRA_WORKER_TYPE, workerTypeName);
		intent.putExtra(PoCService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
		intent.putExtra(PoCService.INTENT_EXTRA_REQUEST_ID, requestId);
		Bundle bundleParams = new Bundle();
		bundleParams.putParcelable(PoCService.INTENT_EXTRA_REQUEST_PARAMS,
				params);
		intent.putExtras(bundleParams);
		mContext.startService(intent);
		mRequestSparseArray.append(requestId, intent);

		return requestId;
	}
}
