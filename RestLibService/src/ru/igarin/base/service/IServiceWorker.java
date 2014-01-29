package ru.igarin.base.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import ru.igarin.base.service.exception.ConnectionException;
import ru.igarin.base.service.exception.CustomRequestException;
import ru.igarin.base.service.exception.DataException;


public interface IServiceWorker {

    public Bundle doWork(Context context, Parcelable intentData)
            throws ConnectionException, DataException, CustomRequestException;

}
