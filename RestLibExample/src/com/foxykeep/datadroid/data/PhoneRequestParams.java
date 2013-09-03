package com.foxykeep.datadroid.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by igarin on 8/19/13.
 */
public class PhoneRequestParams implements Parcelable {

    public String userId;
    public long phoneId;
    public String phoneIds;

    public String toString() {
        return ""+userId;
    }

    public PhoneRequestParams() {
    }

    // Parcelable management
    private PhoneRequestParams(final Parcel in) {
        userId = in.readString();
        phoneId = in.readLong();
        phoneIds = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(userId);
        dest.writeLong(phoneId);
        dest.writeString(phoneIds);
    }

    public static final Parcelable.Creator<PhoneRequestParams> CREATOR = new Parcelable.Creator<PhoneRequestParams>() {
        public PhoneRequestParams createFromParcel(final Parcel in) {
            return new PhoneRequestParams(in);
        }

        public PhoneRequestParams[] newArray(final int size) {
            return new PhoneRequestParams[size];
        }
    };
}
