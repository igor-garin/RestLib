package com.foxykeep.datadroid.data;

import com.google.gson.Gson;

import ru.igarin.base.restlib.provider.annotations.ProviderStoreable;
import ru.igarin.base.restlib.provider.annotations.ProviderStoreableType;

/**
 * Created by igarin on 8/19/13.
 */
@ProviderStoreableType(version=2)
public class Phone {

    @ProviderStoreable
    public long id;

    @ProviderStoreable
    public String name;

    @ProviderStoreable
    public String manufacturer;

    @ProviderStoreable
    public String androidVersion;

    @ProviderStoreable
    public double screenSize;

    @ProviderStoreable
    public int price;

    public static String getPhone(Phone p) {
        return new Gson().toJson(p);
    }

    public static Phone getPhone(String p) {
        return new Gson().fromJson(p, Phone.class);
    }
}
