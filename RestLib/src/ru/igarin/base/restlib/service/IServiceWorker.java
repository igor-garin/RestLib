package ru.igarin.base.restlib.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import ru.igarin.base.restlib.exception.ConnectionException;
import ru.igarin.base.restlib.exception.CustomRequestException;
import ru.igarin.base.restlib.exception.DataException;


public interface IServiceWorker {

    public Bundle doWork(Context context, Parcelable intentData)
            throws ConnectionException, DataException, CustomRequestException;

}
