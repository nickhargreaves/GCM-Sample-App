package com.appsrox.instachat;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appsrox.instachat.client.GcmUtil;
import com.appsrox.instachat.client.ServerUtilities;

/**
 * @author appsrox.com
 *
 */
public class ChatActivity extends Activity implements MessagesFragment.OnFragmentInteractionListener, EditContactDialog.OnFragmentInteractionListener {

	private EditText msgEdit;
	private Button sendBtn;
	private String profileId;
	private String profileName;
	private String profileEmail;
	
	private GcmUtil gcmUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		profileId = getIntent().getStringExtra(Common.PROFILE_ID);
		msgEdit = (EditText) findViewById(R.id.msg_edit);
		sendBtn = (Button) findViewById(R.id.send_btn);
		
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
		if (c.moveToFirst()) {
			profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
			profileEmail = c.getString(c.getColumnIndex(DataProvider.COL_EMAIL));
			actionBar.setTitle(profileName);
		}
		actionBar.setSubtitle("connecting ...");
		
		registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
		gcmUtil = new GcmUtil(getApplicationContext());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			EditContactDialog dialog = new EditContactDialog();
			Bundle args = new Bundle();
			args.putString(Common.PROFILE_ID, profileId);
			args.putString(DataProvider.COL_NAME, profileName);
			dialog.setArguments(args);
			dialog.show(getFragmentManager(), "EditContactDialog");
			return true;
			
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.send_btn:
			send(msgEdit.getText().toString());
			msgEdit.setText(null);
			break;
		}
	}
	
	@Override
	public void onEditContact(String name) {
		getActionBar().setTitle(name);
	}	
	
	@Override
	public String getProfileEmail() {
		return profileEmail;
	}	
	
	private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    ServerUtilities.send(txt, profileEmail);
                    
        			ContentValues values = new ContentValues(2);
        			values.put(DataProvider.COL_MSG, txt);
        			values.put(DataProvider.COL_TO, profileEmail);
        			getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
        			
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	if (!TextUtils.isEmpty(msg)) {
            		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            	}
            }
        }.execute(null, null, null);		
	}	

	@Override
	protected void onPause() {
		ContentValues values = new ContentValues(1);
		values.put(DataProvider.COL_COUNT, 0);
		getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(registrationStatusReceiver);
		gcmUtil.cleanup();
		super.onDestroy();
	}
	
	//--------------------------------------------------------------------------------
	
	private BroadcastReceiver registrationStatusReceiver = new  BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && Common.ACTION_REGISTER.equals(intent.getAction())) {
				switch (intent.getIntExtra(Common.EXTRA_STATUS, 100)) {
				case Common.STATUS_SUCCESS:
					getActionBar().setSubtitle("online");
					sendBtn.setEnabled(true);
					break;
					
				case Common.STATUS_FAILED:
					getActionBar().setSubtitle("offline");					
					break;					
				}
			}
		}
	};	

}
