package ru.igarin.base.restlib.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;

import ru.igarin.base.restlib.exception.RestClientException;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;


public interface IServiceWorker {
	
	public Bundle doWork(Context context, Parcelable intentData) throws IllegalStateException, IOException, URISyntaxException, RestClientException, JSONException;

}
