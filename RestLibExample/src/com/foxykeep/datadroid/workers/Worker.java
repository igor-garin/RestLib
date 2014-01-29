package com.foxykeep.datadroid.workers;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import ru.igarin.base.service.IServiceWorker;
import ru.igarin.base.service.exception.ConnectionException;
import ru.igarin.base.service.exception.CustomRequestException;
import ru.igarin.base.service.exception.DataException;

public class Worker implements IServiceWorker {

	@Override
	public Bundle doWork(Context context, Parcelable intentData)
			throws ConnectionException, DataException, CustomRequestException {
		// TODO Auto-generated method stub
		return null;
	}

}
