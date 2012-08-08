package com.bw.hawksword.ocr;

import com.bw.hawksword.ocr.WordViewActivity;
import com.bw.hawksword.wiktionary.LookupActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;


public class WordhistoryActivity extends Activity {
	private static final String TAG = "WordHistoryActivity";
	static final String[] FROM = {DataAdaptor.C_WORD};
	static final int[] TO = {android.R.id.text1};
	private TabHost tabHost;
	private SharedPreferences prefs;
	private ListView listFavourite,listHistory;
	SimpleCursorAdapter adapter;
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Set tab
		tabHost = (TabHost)findViewById(R.id.tabhost);
		tabHost.setup();
		
		TabSpec spec1 = tabHost.newTabSpec("History");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("History",getResources().getDrawable(android.R.drawable.ic_menu_recent_history));
		
		TabSpec spec2 = tabHost.newTabSpec("Favourite");
		spec2.setContent(R.id.tab2);
		spec2.setIndicator("Favourite",getResources().getDrawable(android.R.drawable.btn_star_big_on));
		
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		
		listFavourite = (ListView) findViewById(R.id.listFavourite);
		listFavourite.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
				final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);	
				//Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				//intent.putExtra("lookup", cursor.getString(1));
				//startActivity(intent);
				
				
			}
		});
		
		listHistory = (ListView) findViewById(R.id.listHistory);
		listHistory.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
		        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);
				//Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				//intent.putExtra("lookup", cursor.getString(1));
				//startActivity(intent);
				
				
			}
		});
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForFavouriteWord();
		adapter = new SimpleCursorAdapter(this,	android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
				//android.R.layout.two_line_list_item, cursor, FROM, TO);
		listFavourite.setAdapter(adapter);
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForHistoryWord();
		adapter = new SimpleCursorAdapter(this,	android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
				//android.R.layout.two_line_list_item, cursor, FROM, TO);
		listHistory.setAdapter(adapter);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		listHistory = (ListView) findViewById(R.id.listHistory);
		listHistory.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
		        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);
				//Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				//intent.putExtra("lookup", cursor.getString(1));
				//startActivity(intent);
				
				
			}
			
		});
		
		listFavourite = (ListView) findViewById(R.id.listFavourite);
		listFavourite.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
		        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);
				//Intent  intent = new Intent(WordhistoryActivity.this, WordViewActivity.class);
				//intent.putExtra("lookup", cursor.getString(1));
				//startActivity(intent);
				
				
			}
			
		});
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForHistoryWord();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
		listHistory.setAdapter(adapter);
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForFavouriteWord();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_expandable_list_item_2, cursor, FROM, TO);
		listFavourite.setAdapter(adapter);
	}

}
