package com.foxykeep.datadroid.workers;

import java.util.HashMap;

import ru.igarin.base.restlib.exception.ConnectionException;
import ru.igarin.base.restlib.exception.CustomRequestException;
import ru.igarin.base.restlib.exception.DataException;
import ru.igarin.base.restlib.network.NetworkConnection;
import ru.igarin.base.restlib.provider.PoCHelper;
import ru.igarin.base.restlib.service.IServiceWorker;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import com.foxykeep.datadroid.config.RestConst;
import com.foxykeep.datadroid.config.ThisProvider;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.dto.PhoneList;
import com.google.gson.Gson;

/**
 * Created by igarin on 8/19/13.
 */
public class WorkerSyncPhoneList implements IServiceWorker {
	@Override
	public Bundle doWork(Context context, Parcelable intentData)
			throws ConnectionException, DataException, CustomRequestException {

		PhoneRequestParams param = (PhoneRequestParams) intentData;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(RestConst.URL_PROPERTY_USER_UDID, param.userId);

		NetworkConnection networkConnection = new NetworkConnection(context,
				RestConst.ROOT_URL_VIEW);
		networkConnection.setParameters(params);

		NetworkConnection.ConnectionResult result = networkConnection.execute();

		PhoneList data = new Gson().fromJson(result.body, PhoneList.class);

		// Clear the table
		PoCHelper.init(ThisProvider.class).setContext(context)
				.setClass(Phone.class).executeDelete();

		// Adds the persons in the database
		final int personListSize = data.phones.phone.length;
		if (data.phones.phone != null && personListSize > 0) {
			ContentValues[] valuesArray = new ContentValues[personListSize];
			for (int i = 0; i < personListSize; i++) {
				valuesArray[i] = PoCHelper
						.getContentValuesImpl(data.phones.phone[i]);
			}
			PoCHelper.init(ThisProvider.class).setContext(context)
					.setClass(Phone.class).setBulkValues(valuesArray)
					.executeBulkInsert();
		}
		return null;
	}
}
