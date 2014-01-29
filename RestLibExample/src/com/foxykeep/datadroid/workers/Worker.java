package com.foxykeep.datadroid.workers;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import ru.igarin.base.restlib.exception.ConnectionException;
import ru.igarin.base.restlib.exception.CustomRequestException;
import ru.igarin.base.restlib.exception.DataException;
import ru.igarin.base.restlib.service.IServiceWorker;

public class Worker implements IServiceWorker {

	@Override
	public Bundle doWork(Context context, Parcelable intentData)
			throws ConnectionException, DataException, CustomRequestException {
		// TODO Auto-generated method stub
		return null;
	}

}
