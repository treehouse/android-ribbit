package com.teamtreehouse.ribbit;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RecipientsActivity extends ListActivity {

	public static final String TAG = RecipientsActivity.class.getSimpleName();

	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;	
	protected List<ParseUser> mFriends;	
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_recipients);
		// Show the Up button in the action bar.
		setupActionBar();
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		setProgressBarIndeterminateVisibility(true);
		
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				
				if (e == null) {
					mFriends = friends;
					
					String[] usernames = new String[mFriends.size()];
					int i = 0;
					for(ParseUser user : mFriends) {
						usernames[i] = user.getUsername();
						i++;
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							getListView().getContext(), 
							android.R.layout.simple_list_item_checked,
							usernames);
					setListAdapter(adapter);
				}
				else {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(e.getMessage())
						.setTitle(R.string.error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipients, menu);
		mSendMenuItem = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_send:
			ParseObject message = createMessage();
			if (message == null) {
				// error
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.error_selecting_file)
					.setTitle(R.string.error_selecting_file_title)
					.setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			else {
				send(message);
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (l.getCheckedItemCount() > 0) {
			mSendMenuItem.setVisible(true);
		}
		else {
			mSendMenuItem.setVisible(false);
		}
	}
	
	protected ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
		
		byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
		
		if (fileBytes == null) {
			return null;
		}
		else {
			if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
				fileBytes = FileHelper.reduceImageForUpload(fileBytes);
			}
			
			String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
			ParseFile file = new ParseFile(fileName, fileBytes);
			message.put(ParseConstants.KEY_FILE, file);

			return message;
		}
	}
	
	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		for (int i = 0; i < getListView().getCount(); i++) {
			if (getListView().isItemChecked(i)) {
				recipientIds.add(mFriends.get(i).getObjectId());
			}
		}
		return recipientIds;
	}
	
	protected void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// success!
					Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(R.string.error_sending_message)
						.setTitle(R.string.error_selecting_file_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}
}






