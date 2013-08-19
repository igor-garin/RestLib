package com.example.ru.mera.restlibexample.rest;

import ru.igarin.base.restlib.provider.ProviderHelper.Requester;
import android.content.Context;

public class PH {
	public static Requester<ThisProvider> from(Context context) {
		return new Requester<ThisProvider>(ThisProvider.class, context);
	}
}
