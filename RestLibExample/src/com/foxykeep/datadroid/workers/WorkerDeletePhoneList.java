package com.foxykeep.datadroid.workers;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import com.foxykeep.datadroid.config.RestConst;
import com.foxykeep.datadroid.config.ThisProvider;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.dto.PhoneList;
import com.google.gson.Gson;

import java.util.HashMap;

import ru.igarin.base.restlib.exception.ConnectionException;
import ru.igarin.base.restlib.exception.CustomRequestException;
import ru.igarin.base.restlib.exception.DataException;
import ru.igarin.base.restlib.network.NetworkConnection;
import ru.igarin.base.restlib.provider.PoCHelper;
import ru.igarin.base.restlib.service.IServiceWorker;

/**
 * Created by igarin on 8/19/13.
 */
public class WorkerDeletePhoneList implements IServiceWorker {
    @Override
    public Bundle doWork(Context context, Parcelable intentData)
            throws ConnectionException, DataException, CustomRequestException {

        PhoneRequestParams param = (PhoneRequestParams) intentData;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(RestConst.URL_PROPERTY_USER_UDID, param.userId);
        params.put(RestConst.URL_PROPERTY_IDS, param.phoneIds);

        NetworkConnection networkConnection = new NetworkConnection(context,
                RestConst.ROOT_URL_DELETE);
        networkConnection.setParameters(params);

        NetworkConnection.ConnectionResult result = networkConnection.execute();

        PhoneList data = new Gson().fromJson(result.body, PhoneList.class);

        for (Phone phone : data.phones.phone) {
        	PoCHelper.init(ThisProvider.class).setContext(context).setClass(Phone.class).setWhere("id" + " = " + phone.id).executeDelete();
        }
        return null;

    }
}
