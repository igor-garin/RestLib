package ru.igarin.base.provider;

import java.util.List;

import ru.igarin.base.provider.annotations.ProviderDataBaseTable;
import ru.igarin.base.provider.config.RestLibLog;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;


public abstract class PoCProvider extends ContentProvider {

	private static final int URI_CASE_BASE = 0;
	private static final int URI_CASE_ALL = URI_CASE_BASE;
	private static final int URI_CASE_ID = URI_CASE_BASE + 1;
	private static int URI_CASE_ALL_INIT_TMP = URI_CASE_ALL;
	private static int URI_CASE_ID_INIT_TMP = URI_CASE_ID;

	static String getAUTHORITY(Class<? extends PoCProvider> provider) {
		return provider.getName();
	}
	
	static Uri getCONTENT_URI(Class<? extends PoCProvider> provider) {
		return Uri.parse("content://" + getAUTHORITY(provider));
	}
	
	static Uri getINTEGRITY_CHECK_URI(Class<? extends PoCProvider> provider) {
		return Uri.parse("content://"
				+ getAUTHORITY(provider) + "/integrityCheck");
	}

	private SQLiteDatabase mDatabase;

	synchronized SQLiteDatabase getDatabase(final Context context,
			Class<? extends Object> classToHandle) {
		// TODO: Always return the cached database, if we've got one
		/*
		 * if (mDatabase != null) { return mDatabase; }
		 */

		final DatabaseHelper helper = new DatabaseHelper(context,
				classToHandle, ProviderHelper.getTableName(classToHandle));
		mDatabase = helper.getWritableDatabase();
		if (mDatabase != null) {
			mDatabase.setLockingEnabled(true);
		}

		return mDatabase;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		private Class<? extends Object> mItem;

		DatabaseHelper(final Context context, Class<? extends Object> item, final String name) {
			super(context, name, null, ProviderHelper.getDBVersion(item));
			mItem = item;
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			RestLibLog.d("Creating database");
			// Creates all tables here; each class has its own method
			RestLibLog.d("PoCProvider | " + ProviderHelper.getTableName(mItem)
					+ " | create table start");
			ProviderHelper.createTable(mItem, db);
			RestLibLog.d("PoCProvider | " + ProviderHelper.getTableName(mItem)
					+ " | create table end");
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			RestLibLog.d("Upgrading database");
			// Creates all tables here; each class has its own method
			RestLibLog.d("PoCProvider | " + ProviderHelper.getTableName(mItem)
					+ " | upgrade table start");
			ProviderHelper.upgradeTable(mItem, db, oldVersion, newVersion);
			RestLibLog.d("PoCProvider | " + ProviderHelper.getTableName(mItem)
					+ " | upgrade table end");
		}

		@Override
		public void onOpen(final SQLiteDatabase db) {
		}
	}

	private static int adaptMatch(int match) {
		return match & 0x0011;
	}

	@Override
	public String getType(final Uri uri) {
		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);

		switch (adaptMatch(match)) {
		case URI_CASE_ID:
			return ProviderHelper.getElemType(classToHandle);
		case URI_CASE_ALL:
			return ProviderHelper.getDirType(classToHandle);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	private class ClassUriMatcher {

		public UriMatcher fun(Class<? extends Object> toHandle) {
			UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

			matcher.addURI(getAUTHORITY(PoCProvider.this.getClass()), toHandle.getName(), URI_CASE_ALL_INIT_TMP);
			// A specific item
			matcher.addURI(getAUTHORITY(PoCProvider.this.getClass()), toHandle.getName() + "/#",
					URI_CASE_ID_INIT_TMP);
			return matcher;
		}
	}

	@Override
	public int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {

		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);

		final Context context = getContext();

		// Pick the correct database for this operation
		final SQLiteDatabase db = getDatabase(context, classToHandle);
		String id = "0";

		RestLibLog.d("delete: uri=" + uri + ", match is " + match);

		int result = -1;

		db.beginTransaction();
		try {
			switch (adaptMatch(match)) {
			case URI_CASE_ID:
				id = uri.getPathSegments().get(1);
				result = db.delete(ProviderHelper.getTableName(classToHandle),
						whereWithId(id, selection), selectionArgs);
				db.setTransactionSuccessful();
				break;
			case URI_CASE_ALL:
				result = db.delete(ProviderHelper.getTableName(classToHandle),
						selection, selectionArgs);
				db.setTransactionSuccessful();
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} finally {
			db.endTransaction();
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {

		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);
		final Context context = getContext();

		// Pick the correct database for this operation
		final SQLiteDatabase db = getDatabase(context, classToHandle);
		long id;

		RestLibLog.d("insert: uri=" + uri + ", match is " + match);

		Uri resultUri = null;

		switch (adaptMatch(match)) {
		case URI_CASE_ALL:
			id = db.insert(ProviderHelper.getTableName(classToHandle), "foo",
					values);
			resultUri = ContentUris.withAppendedId(uri, id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Notify with the base uri, not the new uri (nobody is watching a new
		// record)
		getContext().getContentResolver().notifyChange(uri, null);
		return resultUri;
	}

	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {

		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);
		final Context context = getContext();
		// Pick the correct database for this operation
		final SQLiteDatabase db = getDatabase(context, classToHandle);

		RestLibLog.d("bulkInsert: uri=" + uri + ", match is " + match);

		int numberInserted = 0;
		SQLiteStatement insertStmt;

		db.beginTransaction();
		try {
			switch (adaptMatch(match)) {
			case URI_CASE_ALL:
				insertStmt = db.compileStatement(ProviderHelper
						.getBulkInsertString(classToHandle));
				for (final ContentValues value : values) {
					ProviderHelper.bindValuesInBulkInsert(classToHandle,
							insertStmt, value);
					insertStmt.execute();
					insertStmt.clearBindings();
				}
				insertStmt.close();
				db.setTransactionSuccessful();
				numberInserted = values.length;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} finally {
			db.endTransaction();
		}

		// Notify with the base uri, not the new uri (nobody is watching a new
		// record)
		context.getContentResolver().notifyChange(uri, null);
		return numberInserted;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {

		Cursor c = null;
		final Uri notificationUri = getCONTENT_URI(this.getClass());
		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);
		final Context context = getContext();
		// Pick the correct database for this operation
		final SQLiteDatabase db = getDatabase(context, classToHandle);
		String id;

		RestLibLog.d("query: uri=" + uri + ", match is " + match);

		switch (adaptMatch(match)) {
		case URI_CASE_ID:
			id = uri.getPathSegments().get(1);
			c = db.query(ProviderHelper.getTableName(classToHandle),
					projection, whereWithId(id, selection), selectionArgs,
					null, null, sortOrder);
			break;
		case URI_CASE_ALL:
			c = db.query(ProviderHelper.getTableName(classToHandle),
					projection, selection, selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if ((c != null) && !isTemporary()) {
			c.setNotificationUri(getContext().getContentResolver(),
					notificationUri);
		}
		return c;
	}

	private String whereWithId(final String id, final String selection) {
		
		//TODO: add id functionality
		
		//final StringBuilder sb = new StringBuilder(256);
		//sb.append(BaseColumns._ID); 
		//sb.append(" = ");
		//sb.append(id);
		//if (selection != null) {
		//	sb.append(" AND (");
		//	sb.append(selection);
		//	sb.append(')');
		//}
		//return sb.toString();
		return selection;
	}

	@Override
	public int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {

		Class<? extends Object> classToHandle = getClassFromUri(uri);

		final int match = new ClassUriMatcher().fun(classToHandle).match(uri);
		final Context context = getContext();
		// Pick the correct database for this operation
		final SQLiteDatabase db = getDatabase(context, classToHandle);
		int result;

		RestLibLog.d("update: uri=" + uri + ", match is " + match);

		switch (adaptMatch(match)) {
		case URI_CASE_ID:
			final String id = uri.getPathSegments().get(1);
			result = db.update(ProviderHelper.getTableName(classToHandle),
					values, whereWithId(id, selection), selectionArgs);
			break;
		case URI_CASE_ALL:
			result = db.update(ProviderHelper.getTableName(classToHandle),
					values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	private Class<? extends Object> getClassFromUri(Uri uri) {
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments.isEmpty()) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Class<? extends Object> classToHandle = null;

		try {
			classToHandle = Class.forName(pathSegments.get(0));
		} catch (ClassNotFoundException e) {
			RestLibLog.e(e);
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (!classToHandle.isAnnotationPresent(ProviderDataBaseTable.class)) {
			RestLibLog.e("no annotation");
			throw new IllegalArgumentException(
					"Class is not an ProviderDataBaseTable!");
		}

		return classToHandle;
	}
}
