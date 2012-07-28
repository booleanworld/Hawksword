package com.bw.hawksword.ocr;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

public class WordViewActivity extends Activity {
	TextView wordText;
	CheckBox checkHistory;
	Bundle bundle;
	String lookupWord;
	DataAdaptor wordData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordview);
		
		wordData = ((HawkswordApplication)getApplication()).wordData;
	    
		wordText = (TextView) findViewById(R.id.textShowWord);
		bundle = getIntent().getExtras();
		lookupWord = bundle.getString("lookup");
		wordText.setText(lookupWord);
		
		checkHistory = (CheckBox)findViewById(R.id.checkBoxHistory);
		checkHistory.setChecked(true);
		checkHistory.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(checkHistory.isChecked())
					wordData.insertHistory(lookupWord, new Date(), 0);
				else 
					wordData.delete(lookupWord);
			}
		});
	}
}
