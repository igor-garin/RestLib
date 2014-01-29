package ru.igarin.base.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class ReqResults implements Parcelable {
	Result mResult;
	int requestId;
	int resultCode;
    int statusCode;
	Bundle payload;
	
	ReqResults() {
		
	}
	
	private ReqResults(final Parcel in) {
		mResult = (Result)in.readValue(Result.class.getClassLoader());
		requestId = in.readInt();
		resultCode = in.readInt();
        statusCode = in.readInt();
		payload = in.readBundle();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeValue(mResult);
		dest.writeInt(requestId);
		dest.writeInt(resultCode);
        dest.writeInt(statusCode);
		dest.writeBundle(payload);
	}

	public static final Parcelable.Creator<ReqResults> CREATOR = new Parcelable.Creator<ReqResults>() {
		public ReqResults createFromParcel(final Parcel in) {
			return new ReqResults(in);
		}

		public ReqResults[] newArray(final int size) {
			return new ReqResults[size];
		}
	};
}
