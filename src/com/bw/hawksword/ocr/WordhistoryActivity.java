package com.bw.hawksword.ocr;

import com.bw.hawksword.wiktionary.LookupActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;


public class WordhistoryActivity extends Activity {
	private static final String TAG = "WordHistoryActivity";
	static final String[] FROM = {DataAdaptor.C_WORD,DataAdaptor.C_CREATED_AT};
	static final int[] TO = {android.R.id.text1,android.R.id.text2};
	private TabHost tabHost;
	private SharedPreferences prefs;
	private ListView listFavourite,listHistory;
	SimpleCursorAdapter adapter;
	Cursor cursor;

	// Context menu
	private static final int CLEAR_HID = Menu.FIRST;
	private static final int CLEAR_FID = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		listFavourite = (ListView) findViewById(R.id.listFavourite);
		listHistory = (ListView) findViewById(R.id.listHistory);
		
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
		
	}
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    //    MenuInflater inflater = getMenuInflater();
	    //    inflater.inflate(R.menu.options_menu, menu);
	    super.onCreateOptionsMenu(menu);
	    menu.add(0, CLEAR_HID, 0, "Clear History").setIcon(android.R.drawable.ic_menu_delete);
	    menu.add(1, CLEAR_FID, 1, "Clear Favourite").setIcon(android.R.drawable.ic_menu_delete);
	    return true;
	}
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case CLEAR_HID: {
		    	((HawkswordApplication)getApplication()).wordData.clearAllData("0");
		    	Intent intent = getIntent();
			    overridePendingTransition(0, 0);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    finish();
			    overridePendingTransition(0, 0);
			    startActivity(intent);
		      break;
		    }
		    case CLEAR_FID: {
		    	((HawkswordApplication)getApplication()).wordData.clearAllData("1");
		    	Intent intent = getIntent();
			    overridePendingTransition(0, 0);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    finish();
			    overridePendingTransition(0, 0);
			    startActivity(intent);
		      break;
		    }
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		listHistory = (ListView) findViewById(R.id.listHistory);
		listFavourite = (ListView) findViewById(R.id.listFavourite);
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForHistoryWord();
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, cursor, FROM, TO);
		listHistory.setAdapter(adapter);
		
		cursor = ((HawkswordApplication)getApplication()).wordData.queryForFavouriteWord();
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, cursor, FROM, TO);
		listFavourite.setAdapter(adapter);
		
		listHistory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor = ((HawkswordApplication)getApplication()).wordData.queryForHistoryWord();
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
		        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);				
			}
		});
		
		listFavourite.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				cursor = ((HawkswordApplication)getApplication()).wordData.queryForFavouriteWord();
				cursor.moveToPosition(position);
				String[] dictModes = getResources().getStringArray(R.array.capturemodes);
		        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
				Intent  dict = new Intent(WordhistoryActivity.this,LookupActivity.class);
			    dict.putExtra("ST", cursor.getString(1));
			    dict.putExtra("Mode",dictMode);
			    startActivity(dict);
			}
		});
		
		listFavourite.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				cursor = ((HawkswordApplication)getApplication()).wordData.queryForFavouriteWord();
				cursor.moveToPosition(position);
				((HawkswordApplication)getApplication()).wordData.delete(cursor.getString(1),1);
				Intent intent = getIntent();
			    overridePendingTransition(0, 0);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    finish();

			    overridePendingTransition(0, 0);
			    startActivity(intent);
				return true;
			}
		});
		
		listHistory.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				cursor = ((HawkswordApplication)getApplication()).wordData.queryForHistoryWord();
				cursor.moveToPosition(position);
				((HawkswordApplication)getApplication()).wordData.delete(cursor.getString(1),0);
				Intent intent = getIntent();
			    overridePendingTransition(0, 0);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    finish();

			    overridePendingTransition(0, 0);
			    startActivity(intent);

				return true;
			}
		});
	}
}
