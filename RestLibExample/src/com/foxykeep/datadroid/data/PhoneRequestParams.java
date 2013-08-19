package com.foxykeep.datadroid.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by igarin on 8/19/13.
 */
public class PhoneRequestParams implements Parcelable {

    public String userId;
    public String phone;
    public String phoneIds;

    public void setPhone(Phone p) {
        phone = Phone.getPhone(p);
    }

    public Phone getPhone() {
        return Phone.getPhone(phone);
    }

    public String toString() {
        return ""+userId;
    }

    public PhoneRequestParams() {
    }

    // Parcelable management
    private PhoneRequestParams(final Parcel in) {
        userId = in.readString();
        phone = in.readString();
        phoneIds = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(userId);
        dest.writeString(phone);
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


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((phone == null) ? 0 : phone.hashCode());
        result = prime * result
                + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PhoneRequestParams)) {
            return false;
        }
        PhoneRequestParams other = (PhoneRequestParams) obj;
        if (phone == null) {
            if (other.phone != null) {
                return false;
            }
        } else if (!phone.equals(other.phone)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }
}
