package com.example.ru.mera.restlibexample.rest.data;

import ru.igarin.base.restlib.provider.annotations.ProviderStoreable;
import ru.igarin.base.restlib.provider.annotations.ProviderStoreableType;

@ProviderStoreableType(version = 1)
public class RestData {

	@ProviderStoreable
	public int id;

	@ProviderStoreable
	public String carBrand;

}
