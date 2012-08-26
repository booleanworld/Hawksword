package com.bw.hawksword.ocr;

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
	public static final String TAG = "DbHelperHawskword";

	Context context;
	DbHelper dbHelper;
	SQLiteDatabase db;

	public DataAdaptor(Context context) {
		this.context = context;
		dbHelper = new DbHelper(context, DB_NAME, null, DB_VERSION);
		Log.d(TAG, "WordData constructor");
		
		
	}
	public void insertFavourite(String word,Date date,int rating){
		ContentValues values = new ContentValues();
		db = dbHelper.getWritableDatabase();
		Log.d(TAG, "Favourite WordData insert");

		values.put(C_WORD, word);
		values.put(C_CREATED_AT, date.toString());
		values.put(C_RATING, rating);

		db.insert(TABLE, null, values);
		Log.d(TAG, "SQL Table insert sucess");
	
	}
	public void insertHistory(String word, Date date, int rating) {
		ContentValues values = new ContentValues();
		db = dbHelper.getWritableDatabase();
		Log.d(TAG, "History WordData insert");

		values.put(C_WORD, word);
		values.put(C_CREATED_AT, date.toString());
		values.put(C_RATING, rating);

		db.insert(TABLE, null, values);
		Log.d(TAG, "SQL Table insert sucess");

	}

	public void delete(String word) {
		db = dbHelper.getWritableDatabase();
		
		
		db.delete(TABLE, C_WORD +"=?", new String[] {word});
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
        		Log.i("DataAdaptor",text);
        		Log.i("DataAdaptor",cursor.getString(1).toString());
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
			Log.d(TAG, "SQL Table create ---- ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				Log.d(TAG, "DbHelper Upgrade");
				db.execSQL("drop if exists " + TABLE);
				onCreate(db);
		}

	}
}
