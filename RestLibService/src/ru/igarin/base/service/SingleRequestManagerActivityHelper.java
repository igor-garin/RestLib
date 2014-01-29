package ru.igarin.base.service;

import java.util.EventListener;

import ru.igarin.base.service.RequestManager.OnRequestFinishedListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

public class SingleRequestManagerActivityHelper<T extends PoCService> {

	public static interface RequestListener extends EventListener {

		public void onRequestFinished(int requestType);

		public void onRequestStarted(int requestType);

		public void onRequestDataError(int requestType);

		public void onRequestConnectionError(int requestType, int statusCode);

        public void onRequestCustomError(int requestType, Bundle resultData);

		public void onRequestSuccessed(int requestType,	Bundle payload);
	}
	
	// === public constants ===
	public static final int REQ_TYPE_DEFAULT = 8888;
	
	// === constants ===
	private static final String SAVED_STATE_REQUEST_ID = "SAVED_STATE_REQUEST_ID";
	private static final String SAVED_STATE_REQUEST_TYPE = "SAVED_STATE_REQUEST_TYPE";
	private static final String SAVED_STATE_REQUEST_RES = "SAVED_STATE_REQUEST_RES";
	private static final String SAVED_STATE_REQUEST_SATE = "SAVED_STATE_REQUEST_SATE";
	private static final String SAVED_STATE_REQUEST_PAR = "SAVED_STATE_REQUEST_PAR";

	// === objects to call ===
	private RequestManager mRequestManager;
	private RequestListener mListener = null;

	// === state ===
	private boolean mIsPaused = false;
	private ReqResults mReqResults = null;
	private int mReqType = REQ_TYPE_DEFAULT;
	private int mRequestId = -1;
	private ReqParams mReqParams = null;

	
	// === initializations methods ===
	
	public void setListener(RequestListener listener) {
		mListener = listener;
	}

	public SingleRequestManagerActivityHelper(final Class<T> type,
			final Context context, final Bundle savedInstanceState,
			RequestListener listener) {
		this(type, context.getApplicationContext(), savedInstanceState);
		mListener = listener;
	}

	public SingleRequestManagerActivityHelper(final Class<T> type,
			final Context context, final Bundle savedInstanceState) {
		mRequestManager = RequestManager.from(type,
				context.getApplicationContext());
		if (savedInstanceState != null) {
			mRequestId = savedInstanceState.getInt(SAVED_STATE_REQUEST_ID, -1);
			mReqType = savedInstanceState.getInt(SAVED_STATE_REQUEST_ID,
					REQ_TYPE_DEFAULT);
			mIsPaused = savedInstanceState.getBoolean(SAVED_STATE_REQUEST_SATE);
			mReqResults = savedInstanceState.getParcelable(SAVED_STATE_REQUEST_RES);
			mReqParams = savedInstanceState.getParcelable(SAVED_STATE_REQUEST_PAR);
		}
	}
	
	public void onSaveInstanceState(final Bundle outState) {
		outState.putInt(SAVED_STATE_REQUEST_ID, mRequestId);
		outState.putInt(SAVED_STATE_REQUEST_TYPE, mReqType);
		outState.putBoolean(SAVED_STATE_REQUEST_SATE, mIsPaused);
		outState.putParcelable(SAVED_STATE_REQUEST_RES, mReqResults);
		outState.putParcelable(SAVED_STATE_REQUEST_PAR, mReqParams);
		
	}
	
	// === start methods ===
	
	public void startRequestForWorker(
			final Class<? extends IServiceWorker> workerType,
			final Parcelable params) {
		startRequestForWorker(REQ_TYPE_DEFAULT, workerType, params);
	}

	public void startRequestForWorker(final int reqType,
			final Class<? extends IServiceWorker> workerType,
			final Parcelable params) {
		mReqParams = new ReqParams(reqType, workerType, params);
		mReqType = reqType;
		mRequestManager.addOnRequestFinishedListener(mOnRequestFinishedListener);
		mRequestId = mRequestManager.startRequestForWorker(workerType, params);
		if (mListener != null) {
			mListener.onRequestStarted(mReqType);
		}
	}
	
	public void retryLastRequest() {
		if (mReqParams != null) {
			startRequestForWorker(mReqParams.reqType, mReqParams.workerType,
					mReqParams.getParams());
		}
	}
	
	// === control methods ===
	
	public void onPause() {
		mIsPaused = true;
	}

	public void onResume() {
		mIsPaused = false;
		if (mRequestId != -1) {
			
			if(mReqResults != null) {
				switch(mReqResults.mResult) {
				case Finished:
					mOnRequestFinishedListener.onRequestFinished(mReqResults.requestId, mReqResults.payload);
					break;
				case Error:
					mOnRequestFinishedListener.onRequestDataError(mReqResults.requestId);
					break;
				case Connexion:
					mOnRequestFinishedListener.onRequestConnectionError(mReqResults.requestId, mReqResults.statusCode);
					break;
                case CustomError:
                	mOnRequestFinishedListener.onRequestCustomError(mReqResults.requestId, mReqResults.payload);
                    break;
				}
				mReqResults = null;
			}
			
			if (mRequestManager.isRequestInProgress(mRequestId)) {

			} else {

				mRequestId = -1;
			}
		}
	}
	
	// === call back methods ===
	
	private OnRequestFinishedListener mOnRequestFinishedListener = new OnRequestFinishedListener(){

	    @Override
	    public void onRequestFinished(int requestId, Bundle resultData) {
	        if (requestId == mRequestId) {
	            if (!mIsPaused) {
	                mRequestId = -1;
	                mRequestManager.removeOnRequestFinishedListener(this);
	                if (mListener != null) {
	                    mListener.onRequestFinished(mReqType);
	                    mListener.onRequestSuccessed(mReqType, resultData);
	                }
	            } else {
	                mReqResults = new ReqResults();
	                mReqResults.mResult = Result.Finished;
	                mReqResults.payload = resultData;
	                mReqResults.requestId = requestId;
	            }
	        }
	    }

	    @Override
	    public void onRequestConnectionError(int requestId, int statusCode) {
	        if (requestId == mRequestId) {
	            if (!mIsPaused) {
	                mRequestId = -1;
	                mRequestManager.removeOnRequestFinishedListener(this);
	                if (mListener != null) {
	                    mListener.onRequestFinished(mReqType);
	                    mListener.onRequestConnectionError(mReqType, statusCode);
	                }
	            } else {
	                mReqResults = new ReqResults();
	                mReqResults.mResult = Result.Connexion;
	                mReqResults.statusCode = statusCode;
	                mReqResults.requestId = requestId;
	            }
	        }
	    }

	    @Override
	    public void onRequestDataError(int requestId) {
	        if (requestId == mRequestId) {
	            if (!mIsPaused) {
	                mRequestId = -1;
	                mRequestManager.removeOnRequestFinishedListener(this);
	                if (mListener != null) {
	                    mListener.onRequestFinished(mReqType);
	                    mListener.onRequestDataError(mReqType);
	                }
	            } else {
	                mReqResults = new ReqResults();
	                mReqResults.mResult = Result.Error;
	                mReqResults.requestId = requestId;
	            }
	        }
	    }

	    @Override
	    public void onRequestCustomError(int requestId, Bundle resultData) {
	        if (requestId == mRequestId) {
	            if (!mIsPaused) {
	                mRequestId = -1;
	                mRequestManager.removeOnRequestFinishedListener(this);
	                if (mListener != null) {
	                    mListener.onRequestFinished(mReqType);
	                    mListener.onRequestCustomError(mReqType, resultData);
	                }
	            } else {
	                mReqResults = new ReqResults();
	                mReqResults.mResult = Result.Error;
	                mReqResults.requestId = requestId;
	                mReqResults.payload = resultData;
	            }
	        }
	    }
		
	};

}
