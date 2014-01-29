package ru.igarin.base.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import ru.igarin.base.service.config.RestLibLog;
import ru.igarin.base.service.exception.ConnectionException;
import ru.igarin.base.service.exception.CustomRequestException;
import ru.igarin.base.service.exception.DataException;

/**
 * This class is called by the {@link RequestManager} through the
 * {@link Intent} system. Get the parameters stored in the {@link Intent} and
 * call the right Worker.
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

        final String workerType = intent.getStringExtra(INTENT_EXTRA_WORKER_TYPE);

        RestLibLog.d("workerType:" + workerType);

        try {

            @SuppressWarnings("unchecked")
            Class<? extends IServiceWorker> newClass = (Class<? extends IServiceWorker>) Class.forName(workerType);
            IServiceWorker obj = (IServiceWorker) newClass.newInstance();
            Bundle result = obj.doWork(this, intent.getParcelableExtra(INTENT_EXTRA_REQUEST_PARAMS));
            this.sendSuccess(intent, result);

        } catch (ConnectionException e) {
            RestLibLog.e("ConnectionException", e);
            sendConnexionFailure(intent, e);
        } catch (DataException e) {
            RestLibLog.e("DataException", e);
            sendDataFailure(intent);
        } catch (CustomRequestException e) {
            RestLibLog.e("Custom Exception", e);
            Bundle ret = onCustomRequestException(workerType, e,
                    intent.getParcelableExtra(INTENT_EXTRA_REQUEST_PARAMS));
            sendCustomFailure(intent, ret);
        } catch (ClassNotFoundException e) {
            RestLibLog.e("URISyntaxException", e);
            sendDataFailure(intent);
        } catch (InstantiationException e) {
            RestLibLog.e("URISyntaxException", e);
            sendDataFailure(intent);
        } catch (IllegalAccessException e) {
            RestLibLog.e("URISyntaxException", e);
            sendDataFailure(intent);
        } catch (RuntimeException e) {
            RestLibLog.e("RuntimeException", e);
            sendDataFailure(intent);
        }
    }

    /**
     * Call if a {@link CustomRequestException} is thrown by an {@link IServiceWorker}. You may return a
     * Bundle containing data to return to the {@link RequestManager}.
     * <p/>
     * Default implementation return null. You may want to override this method in your
     * implementation of {@link PoCService} to execute specific action and/or return specific
     * data.
     *
     * @param iServiceWorkerClass The {@link IServiceWorker} which execution threw the exception.
     * @param exception           The {@link CustomRequestException} thrown.
     * @return A {@link Bundle} containing data to return to the {@link RequestManager}. Default
     * implementation return null.
     */
    protected Bundle onCustomRequestException(String iServiceWorkerClass, CustomRequestException exception, Parcelable requestParams) {
        return null;
    }
}
