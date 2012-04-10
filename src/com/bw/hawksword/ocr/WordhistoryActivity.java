package com.bw.hawksword.ocr;

import com.bw.hawksword.ocr.WordViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class WordhistoryActivity extends Activity {
	private static final String TAG = "WordHistoryActivity";
	static final String[] FROM = {WordData.C_WORD};
	static final int[] TO = {android.R.id.text1};
	
	ListView list;
	SimpleCursorAdapter adapter;
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		list = (ListView) findViewById(R.id.listHistory);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				cursor.moveToPosition(position);
				Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				intent.putExtra("lookup", cursor.getString(1));
				startActivity(intent);
				
				
			}
			
		});
		
		cursor = ((HawkswordApplication)getApplication()).wordData.query();

		adapter = new SimpleCursorAdapter(this,	android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
				//android.R.layout.two_line_list_item, cursor, FROM, TO);
		list.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		list = (ListView) findViewById(R.id.listHistory);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				cursor.moveToPosition(position);
				Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				intent.putExtra("lookup", cursor.getString(1));
				startActivity(intent);
				
				
			}
			
		});
		
		cursor = ((HawkswordApplication)getApplication()).wordData.query();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
		list.setAdapter(adapter);
	}

}
