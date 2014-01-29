package ru.igarin.base.service;

import ru.igarin.base.service.config.RestLibLog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class ReqParams implements Parcelable {
	
	int reqType;
	Bundle params;
	Class<? extends IServiceWorker> workerType;
	
	
	ReqParams(final int reqType,
			final Class<? extends IServiceWorker> workerType,
			final Parcelable params) {
		this.reqType = reqType;
		Bundle tmp = new Bundle();
		tmp.putParcelable("params", params);
		this.params = tmp;
		this.workerType = workerType;
		
	}
	
	public Parcelable getParams() {
		return this.params.getParcelable("params");
	}

	@SuppressWarnings("unchecked")
	private ReqParams(final Parcel in) {
		reqType = in.readInt();
		params = in.readBundle();
		try {
			workerType = (Class<? extends IServiceWorker>) Class.forName(in.readString());
		} catch (ClassNotFoundException e) {
			RestLibLog.e(e);
		}
	}


	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(reqType);
		dest.writeBundle(params);
		dest.writeString(workerType.getName());
	}

	public static final ReqParams.Creator<ReqParams> CREATOR = new Parcelable.Creator<ReqParams>() {
		public ReqParams createFromParcel(final Parcel in) {
			return new ReqParams(in);
		}

		public ReqParams[] newArray(final int size) {
			return new ReqParams[size];
		}
	};
}
