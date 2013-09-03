package ru.igarin.base.restlib.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PoCHelper {

	private final Class<? extends PoCProvider> mProvider;
	private Context mContext = null;
	private Class<? extends Object> mClass = null;

	private String[] mProjection = null;
	private String[] mWhereArgs = null;
	private String mSortOrder = null;
	private ContentValues mValues = null;
	private String mWhere = null;
	private ContentValues[] mBulkValues = null;

	private PoCHelper(Class<? extends PoCProvider> provider) {
		mProvider = provider;
	}

	public static PoCHelper init(Class<? extends PoCProvider> provider) {
		return new PoCHelper(provider);
	}

	public static Object getFromCursor(Class<? extends Object> cls, Cursor c) {
		return ProviderHelper.getFromCursor(cls, c);
	}

	public static ContentValues getContentValuesImpl(Object obj) {
		return ProviderHelper.getContentValuesImpl(obj);
	}

	/**
	 * 
	 * Uri maps to the table in the provider for the class.
	 * 
	 */
	public Uri getUri(Class<? extends Object> cls) {
		return ProviderHelper.getUri(mProvider, cls);
	}

	/**
	 * 
	 * Uri maps to the table in the provider for the class.
	 * 
	 */
	public Uri getUri() {
		return ProviderHelper.getUri(mProvider, mClass);
	}

	/**
	 * @param mContext
	 *            the mContext to set
	 */
	public PoCHelper setContext(Context mContext) {
		this.mContext = mContext.getApplicationContext();
		;
		return this;
	}

	/**
	 * @param mClass
	 *            the mClass to set
	 */
	public PoCHelper setClass(Class<? extends Object> mClass) {
		this.mClass = mClass;
		return this;
	}

	/**
	 * @param projection
	 *            is an array of columns that should be included for each row
	 *            retrieved.
	 */
	public PoCHelper setProjection(String[] projection) {
		this.mProjection = projection;
		return this;
	}

	/**
	 * @param whereArgs
	 *            No exact equivalent. Selection arguments replace ?
	 *            placeholders in the selection clause.
	 */
	public PoCHelper setWhereArgs(String[] whereArgs) {
		this.mWhereArgs = whereArgs;
		return this;
	}

	/**
	 * @param sortOrder
	 *            specifies the order in which rows appear in the returned Cursor
	 */
	public PoCHelper setSortOrder(String sortOrder) {
		this.mSortOrder = sortOrder;
		return this;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public PoCHelper setValues(ContentValues values) {
		this.mValues = values;
		return this;
	}

	/**
	 * @param where
	 *            specifies the criteria for selecting rows.
	 */
	public PoCHelper setWhere(String where) {
		this.mWhere = where;
		return this;
	}

	/**
	 * @param bulkValues
	 *            the bulkValues to set
	 */
	public PoCHelper setBulkValues(ContentValues[] bulkValues) {
		this.mBulkValues = bulkValues;
		return this;
	}

	public Cursor executeQuery() {
		if (mContext == null) {
			throw new IllegalArgumentException("mContext was not initialized!");
		}
		return mContext.getContentResolver().query(
				ProviderHelper.getUri(mProvider, mClass), mProjection, mWhere,
				mWhereArgs, mSortOrder);
	}

	public int executeUpdate() {
		if (mContext == null) {
			throw new IllegalArgumentException("mContext was not initialized!");
		}
		return mContext.getContentResolver().update(
				ProviderHelper.getUri(mProvider, mClass), mValues, mWhere,
				mWhereArgs);
	}

	public Uri executeInsert() {
		if (mContext == null) {
			throw new IllegalArgumentException("mContext was not initialized!");
		}
		return mContext.getContentResolver().insert(
				ProviderHelper.getUri(mProvider, mClass), mValues);
	}

	public int executeBulkInsert() {
		if (mContext == null) {
			throw new IllegalArgumentException("mContext was not initialized!");
		}
		return mContext.getContentResolver().bulkInsert(
				ProviderHelper.getUri(mProvider, mClass), mBulkValues);
	}

	public int executeDelete() {
		if (mContext == null) {
			throw new IllegalArgumentException("mContext was not initialized!");
		}
		return mContext.getContentResolver().delete(
				ProviderHelper.getUri(mProvider, mClass), mWhere, mWhereArgs);
	}

}
