package ru.igarin.base.restlib.service;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleReqParams implements Parcelable {

    public static final Parcelable.Creator<SimpleReqParams> CREATOR = new Parcelable.Creator<SimpleReqParams>() {
        public SimpleReqParams createFromParcel(final Parcel in) {
            return new SimpleReqParams(in);
        }

        public SimpleReqParams[] newArray(final int size) {
            return new SimpleReqParams[size];
        }
    };
    public String params;

    public SimpleReqParams() {
    }


    public SimpleReqParams(String value) {
        params = value;
    }

    // Parcelable management
    private SimpleReqParams(final Parcel in) {
        params = in.readString();
    }

    /* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        if (!(obj instanceof SimpleReqParams)) {
            return false;
        }
        SimpleReqParams other = (SimpleReqParams) obj;
        if (params == null) {
            if (other.params != null) {
                return false;
            }
        } else if (!params.equals(other.params)) {
            return false;
        }
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(params);
    }
}
