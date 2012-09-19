package com.bw.hawksword.ocr;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DataAdaptor {
	public static final String DB_NAME = "hawkswords.db";
	public static final int DB_VERSION = 2;

	public static final String TABLE = "words";
	public static final String C_ID = "_id";
	public static final String C_WORD = "word";
	public static final String C_CREATED_AT = "created_at";
	public static final String C_RATING = "rating";

	public static final String TABLE_COUNT = "count";
	public static final String C_IDKEY = "_id";
	public static final String C_VAL = "value";
	public static final String C_FB= "fb";
	public static final String C_TW= "tw";
	public static final String TAG = "DbHelperHawskword";

	Context context;
	DbHelper dbHelper;
	SQLiteDatabase db;

	public DataAdaptor(Context context) {
		this.context = context;
		dbHelper = new DbHelper(context, DB_NAME, null, DB_VERSION);
		Log.d(TAG, "WordData constructor");


	}
	//
	//	public Cursor updateCount(int count, String fb, String tw) {
	//		ContentValues values = new ContentValues();
	//		db = dbHelper.getWritableDatabase();
	//
	//		String sql = String
	//				.format("create table IF NOT EXIST %s "
	//						+ "(%s integer primary key autoincrement, %s text, %s date, %s integer)",
	//						TABLE, C_ID, C_WORD, C_CREATED_AT, C_RATING);
	//
	//		db.execSQL(sql);
	//
	//		values.put(C_IDKEY, 1);
	//		values.put(C_VAL, count);
	//		values.put(C_FB, fb);
	//		values.put(C_TW, tw);
	//		Cursor cursor = db.query(TABLE_COUNT, null, C_IDKEY + "=1", null, null,
	//				null, null);
	//
	//		if (cursor.getCount() == 0) {
	//			db.insert(TABLE_COUNT, null, values);
	//		}
	//
	//		db.update(TABLE_COUNT, values, C_IDKEY + "=1", null);
	//
	//		cursor = db.query(TABLE_COUNT, null, C_IDKEY + "=1", null, null, null,
	//				null);
	//
	//		Log.d(TAG, "SQL Table updated");
	//		return cursor;
	//	}
	public void insertFavourite(String word,Date date,int rating){
		ContentValues values = new ContentValues();
		Calendar myCalendar = Calendar.getInstance();
		db = dbHelper.getWritableDatabase();
		Log.d(TAG, "Favourite WordData insert");

		values.put(C_WORD, word);
		values.put(C_CREATED_AT, myCalendar.get(Calendar.DATE) + "/" + myCalendar.get(Calendar.MONTH) + "/" + myCalendar.get(Calendar.YEAR) + ", " + date.getHours() +":"+ date.getMinutes());
		values.put(C_RATING, rating);

		db.insert(TABLE, null, values);
		Log.d(TAG, "SQL Table insert sucess");

	}
	public void insertHistory(String word,Date date, int rating) {
		ContentValues values = new ContentValues();
		Calendar myCalendar = Calendar.getInstance();
		db = dbHelper.getWritableDatabase();
		Log.d(TAG, "History WordData insert");

		values.put(C_WORD, word);
		values.put(C_CREATED_AT, myCalendar.get(Calendar.DATE) + "/" + myCalendar.get(Calendar.MONTH) + "/" + myCalendar.get(Calendar.YEAR) + ", " + date.getHours() +":"+ date.getMinutes());
		values.put(C_RATING, rating);

		db.insert(TABLE, null, values);

		Log.d(TAG, "SQL Table insert sucess");

	}

	public void delete(String word,int rating) {
		try {
			db = dbHelper.getWritableDatabase();
			db.delete(TABLE, C_WORD +"='" + word + "' and " + C_RATING + "=" + rating, null);
		}
		catch(Exception ex) {
			Log.d(TAG, "SQL Table deletion unsuccessful" + ex.toString());
		}
	}

	public Cursor queryForFavouriteWord() {
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, null, C_RATING + "=1", null, null, null,
				C_ID + " DESC");

		return cursor;
	}
	public Cursor queryForHistoryWord() {
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, null, C_RATING + "=0", null, null, null,C_ID + " DESC");
		return cursor;
	}
	public boolean lookUpHistory(String text,String type){
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, null, C_RATING + "="+type, null, null, null,null);
		cursor.moveToFirst();
		if (cursor != null) 
		{
			cursor.moveToFirst();
			for(int i =0;i<cursor.getCount();i++)
			{	
				if(cursor.getString(1).toString().equalsIgnoreCase(text)){
					return true;
				}
				cursor.moveToNext();
			}

		}
		return false;

	}
	/**
	 * @auther: Maunik Shah(maunik318@gmail.com)
	 * @param type: Pass 0 if you want to clear all History or pass 1 for Favourite
	 * @return this function will return true if list got deleted successfully.
	 */
	public boolean clearAllData(String type) {
		db = dbHelper.getReadableDatabase();
		try {
			db.delete(TABLE,C_RATING + "="+type , null);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	public class DbHelper extends SQLiteOpenHelper {
		Context context;

		public DbHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
			this.context = context;

			Log.d(TAG, "DbHelper constructor");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "DbHelper OnCreate");
			String sql = String
					.format("create table %s "
							+ "(%s integer primary key autoincrement, %s text, %s date, %s integer)",
							TABLE, C_ID, C_WORD, C_CREATED_AT, C_RATING);

			db.execSQL(sql);

			sql = String
					.format("create table %s "
							+ "(%s integer DEFAULT 1, %s integer DEFAULT 5, %s text DEFAULT \'N\', %s text DEFAULT \'N\')",
							TABLE_COUNT, C_IDKEY, C_VAL, C_FB, C_TW );

			db.execSQL(sql);

			Log.d(TAG, "SQL Table create ---- ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "DbHelper Upgrade");
			db.execSQL("drop if exists " + TABLE);
			db.execSQL("drop if exists " + TABLE_COUNT);
			onCreate(db);
		}

	}
}
