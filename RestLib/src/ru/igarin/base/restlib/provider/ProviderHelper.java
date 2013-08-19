package ru.igarin.base.restlib.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

import ru.igarin.base.common.RestLibLog;
import ru.igarin.base.restlib.provider.annotations.ProviderStoreable;
import ru.igarin.base.restlib.provider.annotations.ProviderStoreableType;

public class ProviderHelper {

	private static final String TYPE_ELEM_TYPE = "vnd.android.cursor.item/com.foxykeep.datadroid.data.";
	private static final String TYPE_DIR_TYPE = "vnd.android.cursor.dir/com.foxykeep.datadroid.data.";

	static String getTableName(Class<? extends Object> cls) {
		return cls.getSimpleName().toLowerCase();
	}

	public static Uri getUri(Class<? extends PoCProvider> provider,
			Class<? extends Object> cls) {

		Uri CONTENT_URI = Uri.parse(PoCProvider.getCONTENT_URI(provider) + "/"
				+ cls.getName());
		RestLibLog.d(CONTENT_URI.toString());
		return CONTENT_URI;
	}

	static String getElemType(Class<? extends Object> cls) {
		return TYPE_ELEM_TYPE + getTableName(cls);
	}

	static String getDirType(Class<? extends Object> cls) {
		return TYPE_DIR_TYPE + getTableName(cls);
	}

	static void upgradeTable(Class<? extends Object> cls,
			final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		try {
			db.execSQL("DROP TABLE IF EXISTS " + getTableName(cls));
		} catch (final SQLException e) {
			RestLibLog.e(e);
		}
		createTable(cls, db);
	}
	
	static int getDBVersion(Class<? extends Object> cls) {
		if (!cls.isAnnotationPresent(ProviderStoreableType.class)) {
			RestLibLog.e("no annotation");
			throw new IllegalArgumentException(
					"Class is not an ProviderStoreableType!");
		} else {
			ProviderStoreableType providerStoreableType = cls
					.getAnnotation(ProviderStoreableType.class);
			int version = providerStoreableType.version();
			RestLibLog.d("class annotated ; name  -  " + providerStoreableType
					+ "; version - " + version);
			return version;
		}
	}

	static void createTable(Class<? extends Object> cls, SQLiteDatabase db) {
		StringBuffer sb = new StringBuffer();
		sb.append(" (" + BaseColumns._ID
				+ " integer primary key autoincrement, ");
		ArrayList<AnalyzeHelper.Record> data = AnalyzeHelper.analyze(cls);
		String indexString = "";
		for (AnalyzeHelper.Record r : data) {
			if (r.colum_id != 0) {
				sb.append(r.colum_name).append(" ")
						.append(AnalyzeHelper.getTypesMapping(r.type));
				if (data.size() - 1 != r.colum_id) {
					sb.append(", ");
				} else {
					sb.append(" );");
				}
				if (r.colum_id == 1) {
					indexString = r.colum_name;
				}
			}
		}

		String execSQLString = "create table " + getTableName(cls)
				+ sb.toString();
		RestLibLog.d(execSQLString);
		db.execSQL(execSQLString);

		db.execSQL(DatabaseUtil.getCreateIndexString(getTableName(cls),
				indexString));
	}

	static String getBulkInsertString(Class<? extends Object> cls) {
		final StringBuffer sqlRequest = new StringBuffer("INSERT INTO ");
		final StringBuffer sb = new StringBuffer(" VALUES (");
		sqlRequest.append(getTableName(cls)).append(" ( ");
		ArrayList<AnalyzeHelper.Record> data = AnalyzeHelper.analyze(cls);
		for (AnalyzeHelper.Record r : data) {
			if (r.colum_id != 0) {
				sqlRequest.append(r.colum_name);
				sb.append("?");
				if (data.size() - 1 != r.colum_id) {
					sqlRequest.append(", ");
					sb.append(", ");
				} else {
					sqlRequest.append(" ) ");
					sb.append(")");
				}
			}
		}
		sqlRequest.append(sb.toString());
		RestLibLog.d(sqlRequest.toString());
		return sqlRequest.toString();
	}

	static void bindValuesInBulkInsert(Class<? extends Object> cls,
			SQLiteStatement stmt, ContentValues values) {

		for (AnalyzeHelper.Record r : AnalyzeHelper.analyze(cls)) {
			if (r.colum_id != 0) {
				AnalyzeHelper.bindValues(r, stmt, values);
			}
		}
	}

	public static Object getFromCursor(Class<? extends Object> cls, Cursor c) {
		Object obj = null;
		try {
			obj = cls.newInstance();
			for (AnalyzeHelper.Record r : AnalyzeHelper.analyze(cls)) {
				if (r.colum_id != 0) {
					AnalyzeHelper.set(r, obj, c);
				}
			}
		} catch (InstantiationException e) {
			RestLibLog.e(e);
		} catch (IllegalAccessException e) {
			RestLibLog.e(e);
		}
		return obj;
	}

	public static ContentValues getContentValuesImpl(Object obj) {
		ContentValues values = new ContentValues();
		for (AnalyzeHelper.Record r : AnalyzeHelper.analyze(obj.getClass())) {
			if (r.colum_id != 0) {
				AnalyzeHelper.put(r, obj, values);
			}
		}
		return values;
	}
	
	private static class AnalyzeHelper {
		
		private static class Record {
			private int colum_id = 0;
			private String colum_name = "";
			private RecType type;
			private Field record_field;

			public Record(int id, Field f) {
				colum_id = id;
				record_field = f;
				if (colum_id == 0 && f == null) {
					colum_name = BaseColumns._ID;
					type = RecType.intType;
				} else {
					colum_name = f.getName().toLowerCase();
					type = getType(f);
				}
			}
		}
		
		private enum RecType {
			stringType, intType, longType, doubleType
		}

		private static ArrayList<AnalyzeHelper.Record> analyze(Class<? extends Object> cls) {
			ArrayList<AnalyzeHelper.Record> data = new ArrayList<AnalyzeHelper.Record>();
			data.add(new AnalyzeHelper.Record(0, null));
			Field[] fields = cls.getDeclaredFields();
			for (Field f : fields) {
				Annotation[] annotations = f.getDeclaredAnnotations();
				for(Annotation annotation : annotations){
					if(annotation instanceof ProviderStoreable){
						data.add(new AnalyzeHelper.Record(data.size(), f));
					}
				}
			}
			return data;
		}
		
		private static void set(AnalyzeHelper.Record r, Object obj, Cursor c) {
			try {
				switch (r.type) {
				case stringType:
					r.record_field.set(obj, c.getString(r.colum_id));
					break;
				case intType:
					r.record_field.setInt(obj, c.getInt(r.colum_id));
					break;
				case longType:
					r.record_field.setLong(obj, c.getLong(r.colum_id));
					break;
				case doubleType:
					r.record_field.setDouble(obj, c.getDouble(r.colum_id));
					break;
				}
			} catch (IllegalArgumentException e) {
				RestLibLog.e(e);
			} catch (IllegalAccessException e) {
				RestLibLog.e(e);
			}
		}
		
		private static void put(AnalyzeHelper.Record r, Object obj, ContentValues values) {
			try {
				switch (r.type) {
				case stringType:
					values.put(r.colum_name, (String) r.record_field.get(obj));
					break;
				case intType:
					values.put(r.colum_name, r.record_field.getInt(obj));
					break;
				case longType:
					values.put(r.colum_name, r.record_field.getLong(obj));
					break;
				case doubleType:
					values.put(r.colum_name, r.record_field.getDouble(obj));
					break;
				}
			} catch (IllegalArgumentException e) {
				RestLibLog.e(e);
			} catch (IllegalAccessException e) {
				RestLibLog.e(e);
			}
		}

		private static void bindValues(AnalyzeHelper.Record r, SQLiteStatement stmt,
				ContentValues values) {
			switch (r.type) {
			case stringType:
				String value = values.getAsString(r.colum_name);
				stmt.bindString(r.colum_id, value != null ? value : "");
				break;
			case intType:
				stmt.bindLong(r.colum_id, values.getAsInteger(r.colum_name));
				break;
			case longType:
				stmt.bindLong(r.colum_id, values.getAsLong(r.colum_name));
				break;
			case doubleType:
				stmt.bindDouble(r.colum_id, values.getAsDouble(r.colum_name));
				break;
			}
		}

		private static RecType getType(Field f) {

			if (f.getType().equals(String.class)) {
				return RecType.stringType;
			} else if (f.getType().equals(int.class)) {
				return RecType.intType;
			} else if (f.getType().equals(long.class)) {
				return RecType.longType;
			} else if (f.getType().equals(double.class)) {
				return RecType.doubleType;
			} else {
				throw new RuntimeException(""+f.getType());
			}
		}

		private static String getTypesMapping(RecType type) {
			switch (type) {
			case stringType:
				return "text";
			case intType:
				return "integer";
			case longType:
				return "integer";
			case doubleType:
				return "real";
			default:
				return "none";
			}
		}
	}

	public static class Requester<T extends PoCProvider> {
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
			return context.getContentResolver().query(getUri(provider, cls),
					projection, selection, selectionArgs, sortOrder);
		}

		public static int update(Context context,
				Class<? extends PoCProvider> provider, Class<? extends Object> cls,
				ContentValues values, String where, String[] selectionArgs) {
			return context.getContentResolver().update(getUri(provider, cls),
					values, where, selectionArgs);
		}

		public static Uri insert(Context context,
				Class<? extends PoCProvider> provider, Class<? extends Object> cls,
				ContentValues values) {
			return context.getContentResolver().insert(getUri(provider, cls),
					values);
		}

		public static int bulkInsert(Context context,
				Class<? extends PoCProvider> provider, Class<? extends Object> cls,
				ContentValues[] values) {
			return context.getContentResolver().bulkInsert(
					getUri(provider, cls), values);
		}

		public static int delete(Context context,
				Class<? extends PoCProvider> provider, Class<? extends Object> cls, String where,
				String[] selectionArgs) {
			return context.getContentResolver().delete(getUri(provider, cls),
					where, selectionArgs);
		}
	}
}
