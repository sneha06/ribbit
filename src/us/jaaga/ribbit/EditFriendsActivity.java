package us.jaaga.ribbit;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditFriendsActivity extends ListActivity {

	public static final String TAG = EditFriendsActivity.class.getSimpleName();
	protected List<ParseUser> mUser;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_friends);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		setProgressBarIndeterminate(true);

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.orderByAscending(ParseConstants.KEY_USERNAME);
		query.setLimit(1000);
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> users, ParseException e) {
				setProgressBarIndeterminate(false);
				if (e == null) {
					// success
					mUser = users;
					String[] Usernames = new String[mUser.size()];
					int i = 0;
					for (ParseUser user : mUser) {
						Usernames[i] = user.getUsername();
						i++;
					}

					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							EditFriendsActivity.this,
							android.R.layout.simple_list_item_checked,
							Usernames);
					setListAdapter(adapter);
					 
					addFriendCheckMarks();
				} else {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							EditFriendsActivity.this);
					builder.setMessage(e.getMessage())
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}

			}
		});

	}

	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		if(getListView().isItemChecked(position)){
			//add friends
			mFriendsRelation.add(mUser.get(position));
			
		}else{
			//remove friends
			mFriendsRelation.remove(mUser.get(position));
		}
		mCurrentUser.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e!=null){
					Log.e(TAG, e.getMessage());
				}
				
			}
		});
		
	}
	private void addFriendCheckMarks(){
		mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				if(e == null){
					 //list returned look for a match
					for(int i=0;i<mUser.size();i++){
					ParseUser user = mUser.get(i);
					
					for(ParseUser friend : friends){
						if(friend.getObjectId().equals(user.getObjectId())){
							getListView().setItemChecked(i, true);
						}
					}
					}
				}else{
					Log.e(TAG, e.getMessage());
				}
				
			}
		});
	}

}
