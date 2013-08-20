package com.foxykeep.datadroid.workers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;

import com.foxykeep.datadroid.config.PH;
import com.foxykeep.datadroid.config.RestConst;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.dto.PhoneList;
import com.google.gson.Gson;

import java.util.HashMap;

import ru.igarin.base.restlib.exception.ConnectionException;
import ru.igarin.base.restlib.exception.CustomRequestException;
import ru.igarin.base.restlib.exception.DataException;
import ru.igarin.base.restlib.network.NetworkConnection;
import ru.igarin.base.restlib.service.IServiceWorker;

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
        PH.from(context).delete(Phone.class, null, null);

        // Adds the persons in the database
        final int personListSize = data.phones.phone.length;
        if (data.phones.phone != null && personListSize > 0) {
            ContentValues[] valuesArray = new ContentValues[personListSize];
            for (int i = 0; i < personListSize; i++) {
                valuesArray[i] = PH.from(context).getContentValuesImpl(data.phones.phone[i]);
            }
            PH.from(context).bulkInsert(Phone.class, valuesArray);
        }
        return null;
    }
}
