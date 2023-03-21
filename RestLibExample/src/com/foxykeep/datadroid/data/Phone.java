package com.foxykeep.datadroid.data;

import ru.igarin.base.provider.annotations.ProviderDataBaseColumn;
import ru.igarin.base.provider.annotations.ProviderDataBaseTable;

/**
 * Updated by igarin on 21/4/23.
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

}
