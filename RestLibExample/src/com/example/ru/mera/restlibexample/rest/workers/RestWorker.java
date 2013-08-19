package com.example.ru.mera.restlibexample.rest.workers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import ru.igarin.base.restlib.exception.RestClientException;
import ru.igarin.base.restlib.service.IServiceWorker;

public class RestWorker implements IServiceWorker {

	@Override
	public Bundle doWork(Context arg0, Parcelable arg1)
			throws IllegalStateException, IOException, URISyntaxException,
			RestClientException, JSONException {
		// TODO Auto-generated method stub
		return null;
	}

}
