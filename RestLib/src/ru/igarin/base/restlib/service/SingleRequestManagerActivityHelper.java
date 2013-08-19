package ru.igarin.base.restlib.service;

import java.util.EventListener;

import ru.igarin.base.common.RestLibLog;
import ru.igarin.base.restlib.service.RequestManager.OnRequestFinishedListener;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SingleRequestManagerActivityHelper<T extends PoCService>
		implements OnRequestFinishedListener {
	
	private static class ReqParams implements Parcelable {
		
		private int reqType;
		private Bundle params;
		private Class<? extends IServiceWorker> workerType;
		
		
		private ReqParams(final int reqType,
				final Class<? extends IServiceWorker> workerType,
				final Parcelable params) {
			this.reqType = reqType;
			Bundle tmp = new Bundle();
			tmp.putParcelable("params", params);
			this.params = tmp;
			this.workerType = workerType;
			
		}
		
		public Parcelable getParams() {
			return this.params.getParcelable("params");
		}

		@SuppressWarnings("unchecked")
		private ReqParams(final Parcel in) {
			reqType = in.readInt();
			params = in.readBundle();
			try {
				workerType = (Class<? extends IServiceWorker>) Class.forName(in.readString());
			} catch (ClassNotFoundException e) {
				RestLibLog.e(e);
			}
		}


		public int describeContents() {
			return 0;
		}

		public void writeToParcel(final Parcel dest, final int flags) {
			dest.writeInt(reqType);
			dest.writeBundle(params);
			dest.writeString(workerType.getName());
		}

		@SuppressWarnings("unused")
		public static final ReqParams.Creator<ReqParams> CREATOR = new Parcelable.Creator<ReqParams>() {
			public ReqParams createFromParcel(final Parcel in) {
				return new ReqParams(in);
			}

			public ReqParams[] newArray(final int size) {
				return new ReqParams[size];
			}
		};
	}
	
	private static class ReqResults implements Parcelable {
		private Result mResult;
		private int requestId;
		private int resultCode;
		private Bundle payload;
		
		private ReqResults() {
			
		}
		
		private ReqResults(final Parcel in) {
			mResult = (Result)in.readValue(Result.class.getClassLoader());
			requestId = in.readInt();
			resultCode = in.readInt();
			payload = in.readBundle();
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(final Parcel dest, final int flags) {
			dest.writeValue(mResult);
			dest.writeInt(requestId);
			dest.writeInt(requestId);
			dest.writeBundle(payload);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<ReqResults> CREATOR = new Parcelable.Creator<ReqResults>() {
			public ReqResults createFromParcel(final Parcel in) {
				return new ReqResults(in);
			}

			public ReqResults[] newArray(final int size) {
				return new ReqResults[size];
			}
		};
	}

	public static interface RequestListener extends EventListener {

		public void onRequestFinished(int requestType);

		public void onRequestStarted(int requestType);

		public void onHandleError(int requestType, int resultCode, Bundle payload);

		public void onConnexionError(int requestType, int resultCode,
				Bundle payload);

		public void onRequestSuccessed(int requestType, int resultCode,
				Bundle payload);

	}

	private enum Result {
		Finished, Error, Connexion
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
		mRequestManager.addOnRequestFinishedListener(this);
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
					onRequestFinished(mReqResults.requestId, mReqResults.resultCode, mReqResults.payload);
					break;
				case Error:
					onHandleError(mReqResults.requestId, mReqResults.resultCode, mReqResults.payload);
					break;
				case Connexion:
					onConnexionError(mReqResults.requestId, mReqResults.resultCode, mReqResults.payload);
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

	@Override
	public void onRequestFinished(int requestId, int resultCode, Bundle payload) {
		if (requestId == mRequestId) {
			if (!mIsPaused) {
				mRequestId = -1;
				mRequestManager.removeOnRequestFinishedListener(this);
				if (mListener != null) {
					mListener.onRequestFinished(mReqType);
					mListener.onRequestSuccessed(mReqType, resultCode, payload);
				}
			} else {
				mReqResults = new ReqResults();
				mReqResults.mResult = Result.Finished;
				mReqResults.payload = payload;
				mReqResults.requestId = requestId;
				mReqResults.resultCode = resultCode;
			}
		}
	}

	@Override
	public void onHandleError(int requestId, int resultCode, Bundle payload) {
		if (requestId == mRequestId) {
			if (!mIsPaused) {
				mRequestId = -1;
				mRequestManager.removeOnRequestFinishedListener(this);
				if (mListener != null) {
					mListener.onRequestFinished(mReqType);
					mListener.onHandleError(mReqType, resultCode, payload);
				}
			} else {
				mReqResults = new ReqResults();
				mReqResults.mResult = Result.Error;
				mReqResults.payload = payload;
				mReqResults.requestId = requestId;
				mReqResults.resultCode = resultCode;
			}
		}
	}

	@Override
	public void onConnexionError(int requestId, int resultCode, Bundle payload) {
		if (requestId == mRequestId) {
			if (!mIsPaused) {
				mRequestId = -1;
				mRequestManager.removeOnRequestFinishedListener(this);
				if (mListener != null) {
					mListener.onRequestFinished(mReqType);
					mListener.onConnexionError(mReqType, resultCode, payload);
				}
			} else {
				mReqResults = new ReqResults();
				mReqResults.mResult = Result.Connexion;
				mReqResults.payload = payload;
				mReqResults.requestId = requestId;
				mReqResults.resultCode = resultCode;
			}
		}
	}
}
