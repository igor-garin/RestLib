package ru.igarin.base.restlib.service;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * This class is nearly identical to {@link IntentService}. The only difference
 * is that the subclasses can specify the number of parallel working thread.
 * 
 */
abstract class MultiThreadService extends Service {

    private ExecutorService mThreadPool;
    private int mMaxThreads;

    @SuppressWarnings("rawtypes")
    private ArrayList<Future> mFutureList;

    private Handler mHandler;

    public MultiThreadService(final int maxThreads) {
        mMaxThreads = maxThreads;
    }

    /**
     * Callback used when a thread has finished working
     */
    final Runnable mHasFinishedWorkingRunnable = new Runnable() {

        @SuppressWarnings("rawtypes")
        public void run() {
            final ArrayList<Future> futureList = mFutureList;
            for (int i = 0; i < futureList.size(); i++) {
                if (futureList.get(i).isDone()) {
                    futureList.remove(i);
                    i--;
                }
            }

            if (futureList.isEmpty()) {
                stopSelf();
            }
        }
    };

    @SuppressWarnings("rawtypes")
    @Override
    public void onCreate() {
        super.onCreate();
        mThreadPool = Executors.newFixedThreadPool(mMaxThreads);
        mHandler = new Handler();
        mFutureList = new ArrayList<Future>();
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        IntentRunnable runnable = new IntentRunnable(intent);
        mFutureList.add(mThreadPool.submit(runnable));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private class IntentRunnable implements Runnable {
        private Intent mIntent;

        public IntentRunnable(final Intent intent) {
            mIntent = intent;
        }

        public void run() {
            onHandleIntent(mIntent);
            mHandler.post(mHasFinishedWorkingRunnable);
        }
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * The processing happens on a worker thread that runs independently from
     * other application logic.
     * 
     * @param intent The value passed to {@link Content#startService(Intent)}.
     */
    abstract protected void onHandleIntent(Intent intent);
}
