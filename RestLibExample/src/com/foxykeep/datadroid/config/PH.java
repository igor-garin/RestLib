package com.foxykeep.datadroid.config;

import android.content.Context;

import ru.igarin.base.restlib.provider.Requester;

public class PH {

	public static Requester<ThisProvider> from(Context context) {
		return new Requester<ThisProvider>(ThisProvider.class, context);
	}
}
