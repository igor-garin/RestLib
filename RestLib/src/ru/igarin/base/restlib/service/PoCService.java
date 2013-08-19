package ru.igarin.base.restlib.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;

import ru.igarin.base.common.RestLibLog;
import ru.igarin.base.restlib.exception.RestClientException;
import ru.igarin.base.restlib.network.NetworkConnection;
import android.content.Intent;
import android.os.Bundle;

/**
 * This class is called by the {@link RequestManager} through the
 * {@link Intent} system. Get the parameters stored in the {@link Intent} and
 * call the right Worker.
 * 
 */
public abstract class PoCService extends WorkerService {
	

	// Max number of parallel threads used
	private static final int MAX_THREADS = 3;

	public PoCService() {
		super(MAX_THREADS);
		RestLibLog.d("PoCService created");
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		
		RestLibLog.d("onHandleIntent");
		// This line will generate the Android User Agent which will be used in
		// your webservice calls if you don't specify a special one
		NetworkConnection.generateDefaultUserAgent(this);

		final String workerType = intent.getStringExtra(INTENT_EXTRA_WORKER_TYPE);

		RestLibLog.d("workerType:"+workerType);
		
		try {
			
			@SuppressWarnings("unchecked")
			Class<? extends IServiceWorker> newClass = (Class<? extends IServiceWorker>) Class.forName(workerType);
			IServiceWorker obj = (IServiceWorker)newClass.newInstance();
			Bundle result = obj.doWork(this,  intent.getParcelableExtra(INTENT_EXTRA_REQUEST_PARAMS));
			this.sendSuccess(intent, result);

        } catch (final IllegalStateException e) {
            RestLibLog.e("IllegalStateException", e);
            sendConnexionFailure(intent, null);
        } catch (final IOException e) {
            RestLibLog.e("IOException", e);
            sendConnexionFailure(intent, null);
        } catch (final URISyntaxException e) {
            RestLibLog.e("URISyntaxException", e);
            sendConnexionFailure(intent, null);
        } catch (final RestClientException e) {
            RestLibLog.e("RestClientException", e);
            Bundle data = new Bundle();
            data.putInt(RequestManager.INTENT_EXTRA_ERROR_STATUS, e.getErrorStatus());
            sendConnexionFailure(intent, data);
        } /*catch (final ParserConfigurationException e) {
            Log.e(LOG_TAG, "ParserConfigurationException", e);
            sendDataFailure(intent, null);
        } catch (final SAXException e) {
            Log.e(LOG_TAG, "SAXException", e);
            sendDataFailure(intent, null);
        } */catch (final JSONException e) {
            RestLibLog.e("JSONException", e);
            sendDataFailure(intent, null);
        } catch (ClassNotFoundException e) {
			RestLibLog.e("ClassNotFoundException", e);
			sendDataFailure(intent, null);
		} catch (InstantiationException e) {
			RestLibLog.e("InstantiationException", e);
			sendDataFailure(intent, null);
		} catch (IllegalAccessException e) {
			RestLibLog.e("IllegalAccessException", e);
			sendDataFailure(intent, null);
		}
		// This block (which should be the last one in your implementation)
		// will catch all the RuntimeException and send you back an error
		// that you can manage. If you remove this catch, the
		// RuntimeException will still crash the PoCService but you will not be
		// informed (as it is in 'background') so you should never remove this
		// catch
		catch (final RuntimeException e) {
			RestLibLog.e("RuntimeException", e);
			sendDataFailure(intent, null);
		} 
	}
}
