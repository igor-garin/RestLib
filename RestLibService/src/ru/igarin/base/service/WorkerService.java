package ru.igarin.base.service;

import ru.igarin.base.service.config.RestLibLog;
import ru.igarin.base.service.exception.ConnectionException;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;


/**
 * This class is the superclass of all the worker service you'll create.
 * 
 */
abstract class WorkerService extends MultiThreadService {

	public static final String INTENT_EXTRA_WORKER_TYPE = "com.foxykeep.datadroid.extras.workerType";
	public static final String INTENT_EXTRA_WORKER = "com.foxykeep.datadroid.extras.worker";
	public static final String INTENT_EXTRA_REQUEST_ID = "com.foxykeep.datadroid.extras.requestId";
	public static final String INTENT_EXTRA_RECEIVER = "com.foxykeep.datadroid.extras.receiver";
	public static final String INTENT_EXTRA_REQUEST_PARAMS = "com.foxykeep.datadroid.extras.params";

	public static final int SUCCESS_CODE = 0;
	public static final int ERROR_CODE = -1;

	public WorkerService(final int maxThreads) {
		super(maxThreads);
	}

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    /**
	 * Proxy method for {@link #sendResult(Intent, Bundle, int)} when the work
	 * is a success
	 * 
	 * @param intent
	 *            The value passed to {@link onHandleIntent(Intent)}.
	 * @param data
	 *            A {@link Bundle} with the data to send back
	 */
	protected void sendSuccess(final Intent intent, final Bundle data) {
		sendResult(intent, data, SUCCESS_CODE);
	}

	/**
	 * Proxy method for {@link #sendResult(Intent, Bundle, int)} when the work
	 * is a failure
	 * 
	 * @param intent
	 *            The value passed to {@link onHandleIntent(Intent)}.
	 * @param data
	 *            A {@link Bundle} the data to send back
	 */
	protected void sendFailure(final Intent intent, final Bundle data) {
		sendResult(intent, data, ERROR_CODE);
	}

	/**
	 * Proxy method for {@link #sendResult(Intent, Bundle, int)} when the work
	 * is a failure due to the network
	 * 
	 * @param intent
	 *            The value passed to {@link onHandleIntent(Intent)}.
	 * @param data
	 *            A {@link Bundle} the data to send back
	 */
	protected void sendConnexionFailure(final Intent intent, ConnectionException exception) {
        Bundle data = new Bundle();
        data.putInt(RequestManager.RECEIVER_EXTRA_ERROR_TYPE, RequestManager.RECEIVER_EXTRA_VALUE_ERROR_TYPE_CONNEXION);
        data.putInt(RequestManager.RECEIVER_EXTRA_CONNECTION_ERROR_STATUS_CODE,
                exception.getStatusCode());
		sendResult(intent, data, ERROR_CODE);
	}

	/**
	 * Proxy method for {@link #sendResult(Intent, Bundle, int)} when the work
	 * is a failure due to the data (parsing for example)
	 * 
	 * @param intent
	 *            The value passed to {@link onHandleIntent(Intent)}.
	 * @param data
	 *            A {@link Bundle} the data to send back
	 */
	protected void sendDataFailure(final Intent intent) {
        Bundle data = new Bundle();
        data.putInt(RequestManager.RECEIVER_EXTRA_ERROR_TYPE, RequestManager.RECEIVER_EXTRA_VALUE_ERROR_TYPE_DATA);
		sendResult(intent, data, ERROR_CODE);
	}

    /**
     * Proxy method for {@link #sendResult(ResultReceiver, Bundle, int)} when the work is a failure
     * due to {@link CustomRequestException} being thrown.
     *
     * @param receiver The result receiver received inside the {@link Intent}.
     * @param data A {@link Bundle} the data to send back.
     */
    protected void sendCustomFailure(final Intent intent, Bundle data) {
        if (data == null) {
            data = new Bundle();
        }
        data.putInt(RequestManager.RECEIVER_EXTRA_ERROR_TYPE, RequestManager.RECEIVER_EXTRA_VALUE_ERROR_TYPE_CUSTOM);
        sendResult(intent, data, ERROR_CODE);
    }

	/**
	 * Method used to send back the result to the {@link RequestManager}
	 * 
	 * @param intent
	 *            The value passed to {@link onHandleIntent(Intent)}. It must
	 *            contain the {@link ResultReceiver} and the requestId
	 * @param data
	 *            A {@link Bundle} the data to send back
	 * @param code
	 *            The sucess/error code to send back
	 */
	protected void sendResult(final Intent intent, Bundle data, final int code) {

		RestLibLog.d("sendResult : "
				+ ((code == SUCCESS_CODE) ? "Success" : "Failure"));

		ResultReceiver receiver = (ResultReceiver) intent
				.getParcelableExtra(INTENT_EXTRA_RECEIVER);

		if (receiver != null) {
			if (data == null) {
				data = new Bundle();
			}

			data.putInt(RequestManager.RECEIVER_EXTRA_REQUEST_ID,
					intent.getIntExtra(INTENT_EXTRA_REQUEST_ID, -1));

			receiver.send(code, data);
		}
	}
}
