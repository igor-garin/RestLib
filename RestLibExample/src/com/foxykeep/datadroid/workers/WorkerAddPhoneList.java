package com.foxykeep.datadroid.workers;

import java.util.HashMap;

import ru.igarin.base.provider.PoCHelper;
import ru.igarin.base.service.IServiceWorker;
import ru.igarin.base.service.exception.ConnectionException;
import ru.igarin.base.service.exception.CustomRequestException;
import ru.igarin.base.service.exception.DataException;
import ru.igarin.base.service.network.NetworkConnection;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;

import com.foxykeep.datadroid.config.RestConst;
import com.foxykeep.datadroid.config.ThisProvider;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.dto.PhoneFromServer;
import com.google.gson.Gson;

/**
 * Created by igarin on 8/19/13.
 */
public class WorkerAddPhoneList implements IServiceWorker {
	@Override
	public Bundle doWork(Context context, Parcelable intentData)
			throws ConnectionException, DataException, CustomRequestException {

		PhoneRequestParams param = (PhoneRequestParams) intentData;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(RestConst.URL_PROPERTY_USER_UDID, param.userId);

		Cursor c = PoCHelper.init(ThisProvider.class).setContext(context)
				.setClass(Phone.class).setWhere("id = " + param.phoneId)
				.executeQuery();
		c.moveToFirst();
		Phone phone = (Phone) PoCHelper.getFromCursor(Phone.class, c);

		params.put(RestConst.URL_PROPERTY_NAME, phone.name);
		params.put(RestConst.URL_PROPERTY_MANUFACTURER, phone.manufacturer);
		params.put(RestConst.URL_PROPERTY_ANDROID_VERSION, phone.androidVersion);
		params.put(RestConst.URL_PROPERTY_SCREEN_SIZE,
				String.valueOf(phone.screenSize));
		params.put(RestConst.URL_PROPERTY_PRICE, String.valueOf(phone.price));

		NetworkConnection networkConnection = new NetworkConnection(context,
				RestConst.ROOT_URL_ADD_EDIT);
		networkConnection.setParameters(params);

		NetworkConnection.ConnectionResult result = networkConnection.execute();

		PhoneFromServer serverResponse = new Gson().fromJson(result.body,
				PhoneFromServer.class);
		ContentValues values = PoCHelper
				.getContentValuesImpl(serverResponse.phone);

		PoCHelper.init(ThisProvider.class).setContext(context)
				.setClass(Phone.class).setValues(values)
				.setWhere("id" + " = " + serverResponse.phone.id)
				.executeUpdate();

		return null;
	}
}
