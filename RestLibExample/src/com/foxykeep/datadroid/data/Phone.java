package com.foxykeep.datadroid.data;

import com.google.gson.Gson;

import ru.igarin.base.restlib.provider.annotations.ProviderDataBaseColumn;
import ru.igarin.base.restlib.provider.annotations.ProviderDataBaseTable;

/**
 * Created by igarin on 8/19/13.
 */
@ProviderDataBaseTable(version=14)
public class Phone {

    @ProviderDataBaseColumn
    public long id;

    @ProviderDataBaseColumn
    public String name;

    @ProviderDataBaseColumn
    public String manufacturer;

    @ProviderDataBaseColumn
    public String androidVersion;

    @ProviderDataBaseColumn
    public double screenSize;

    @ProviderDataBaseColumn
    public int price;
    
    @ProviderDataBaseColumn
    public int TMP;

    public static String getPhone(Phone p) {
        return new Gson().toJson(p);
    }

    public static Phone getPhone(String p) {
        return new Gson().fromJson(p, Phone.class);
    }
}
