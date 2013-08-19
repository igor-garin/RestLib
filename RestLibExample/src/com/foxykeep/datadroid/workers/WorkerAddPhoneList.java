package com.foxykeep.datadroid.workers;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import com.foxykeep.datadroid.config.Lg;
import com.foxykeep.datadroid.config.PH;
import com.foxykeep.datadroid.config.RestConst;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.dto.PhoneFromServer;
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
public class WorkerAddPhoneList implements IServiceWorker {
    @Override
    public Bundle doWork(Context context, Parcelable intentData)
            throws ConnectionException, DataException, CustomRequestException {

        PhoneRequestParams param = (PhoneRequestParams) intentData;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(RestConst.URL_PROPERTY_USER_UDID, param.userId);
        params.put(RestConst.URL_PROPERTY_NAME, param.getPhone().name);
        params.put(RestConst.URL_PROPERTY_MANUFACTURER,
                param.getPhone().manufacturer);
        params.put(RestConst.URL_PROPERTY_ANDROID_VERSION,
                param.getPhone().androidVersion);
        params.put(RestConst.URL_PROPERTY_SCREEN_SIZE,
                String.valueOf(param.getPhone().screenSize));
        params.put(RestConst.URL_PROPERTY_PRICE,
                String.valueOf(param.getPhone().price));

        NetworkConnection networkConnection = new NetworkConnection(context,
                RestConst.ROOT_URL_ADD_EDIT);
        networkConnection.setParameters(params);

        NetworkConnection.ConnectionResult result = networkConnection.execute();

        PhoneFromServer phone = new Gson().fromJson(result.body, PhoneFromServer.class);
        ContentValues values = PH.from(context).getContentValuesImpl(phone.phone);
        PH.from(context).insert(Phone.class, values);

        return null;
    }
}
