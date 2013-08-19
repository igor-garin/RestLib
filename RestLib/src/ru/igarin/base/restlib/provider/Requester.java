package ru.igarin.base.restlib.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by igarin on 8/19/13.
 */
public class Requester<T extends PoCProvider> {
    private final Class<T> mProviderClass;
    private final Context mContext;

    public Requester(Class<T> providerClass, final Context context) {
        this.mProviderClass = providerClass;
        mContext = context.getApplicationContext();
    }

    public Requester(Class<T> providerClass) {
        this.mProviderClass = providerClass;
        mContext = null;
    }


    public static Object getFromCursor(Class<? extends Object> cls, Cursor c) {
        return ProviderHelper.getFromCursor(cls, c);
    }

    public static ContentValues getContentValuesImpl(Object obj) {
        return ProviderHelper.getContentValuesImpl(obj);
    }

    public Cursor query(Class<? extends Object> cls, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (mContext == null) {
            throw new IllegalArgumentException(
                    "mContext was not initialized!");
        }
        return query(mContext, mProviderClass, cls, projection, selection,
                selectionArgs, sortOrder);
    }

    public int update(Class<? extends Object> cls, ContentValues values, String where,
                      String[] selectionArgs) {
        if (mContext == null) {
            throw new IllegalArgumentException(
                    "mContext was not initialized!");
        }
        return update(mContext, mProviderClass, cls, values, where,
                selectionArgs);
    }

    public Uri insert(Class<? extends Object> cls, ContentValues values) {
        if (mContext == null) {
            throw new IllegalArgumentException(
                    "mContext was not initialized!");
        }
        return insert(mContext, mProviderClass, cls, values);
    }

    public int bulkInsert(Class<? extends Object> cls, ContentValues[] values) {
        if (mContext == null) {
            throw new IllegalArgumentException(
                    "mContext was not initialized!");
        }
        return bulkInsert(mContext, mProviderClass, cls, values);
    }

    public int delete(Class<? extends Object> cls, String where, String[] selectionArgs) {
        if (mContext == null) {
            throw new IllegalArgumentException(
                    "mContext was not initialized!");
        }
        return delete(mContext, mProviderClass, cls, where, selectionArgs);
    }

    // ================================
    public Cursor query(Context context, Class<? extends Object> cls, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        return query(context, mProviderClass, cls, projection, selection,
                selectionArgs, sortOrder);
    }

    public int update(Context context, Class<? extends Object> cls, ContentValues values,
                      String where, String[] selectionArgs) {
        return update(context, mProviderClass, cls, values, where,
                selectionArgs);
    }

    public Uri insert(Context context, Class<? extends Object> cls, ContentValues values) {
        return insert(context, mProviderClass, cls, values);
    }

    public int bulkInsert(Context context, Class<? extends Object> cls, ContentValues[] values) {
        return bulkInsert(context, mProviderClass, cls, values);
    }

    public int delete(Context context, Class<? extends Object> cls, String where,
                      String[] selectionArgs) {
        return delete(context, mProviderClass, cls, where, selectionArgs);
    }

    // ================================================
    public static Cursor query(Context context,
                               Class<? extends PoCProvider> provider, Class<? extends Object> cls,
                               String[] projection, String selection, String[] selectionArgs,
                               String sortOrder) {
        return context.getContentResolver().query(ProviderHelper.getUri(provider, cls),
                projection, selection, selectionArgs, sortOrder);
    }

    public static int update(Context context,
                             Class<? extends PoCProvider> provider, Class<? extends Object> cls,
                             ContentValues values, String where, String[] selectionArgs) {
        return context.getContentResolver().update(ProviderHelper.getUri(provider, cls),
                values, where, selectionArgs);
    }

    public static Uri insert(Context context,
                             Class<? extends PoCProvider> provider, Class<? extends Object> cls,
                             ContentValues values) {
        return context.getContentResolver().insert(ProviderHelper.getUri(provider, cls),
                values);
    }

    public static int bulkInsert(Context context,
                                 Class<? extends PoCProvider> provider, Class<? extends Object> cls,
                                 ContentValues[] values) {
        return context.getContentResolver().bulkInsert(
                ProviderHelper.getUri(provider, cls), values);
    }

    public static int delete(Context context,
                             Class<? extends PoCProvider> provider, Class<? extends Object> cls, String where,
                             String[] selectionArgs) {
        return context.getContentResolver().delete(ProviderHelper.getUri(provider, cls),
                where, selectionArgs);
    }
}
