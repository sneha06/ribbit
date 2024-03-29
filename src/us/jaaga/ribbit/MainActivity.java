package us.jaaga.ribbit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	public static final int TAKE_PHOTO_RQUEST = 0;
	public static final int TAKE_VIDEO_RQUEST = 1;
	public static final int PICK_PHOTO_RQUEST = 2;
	public static final int PICK_VIDEO_RQUEST = 3;
	
	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	
	public static final int FILE_SIZE_LIMIT = 1024*1024*10; //10MB
	
	protected Uri mMediaUri;
	
	
	protected DialogInterface.OnClickListener mDialogListner = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which)
			{
				case 0://Take picture
					Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mMediaUri = getoutputMediaFileUri(MEDIA_TYPE_IMAGE);
					if(mMediaUri==null){
						//display an error
						Toast.makeText(MainActivity.this,"there was a problem acceccing your device's external storage", Toast.LENGTH_LONG).show();
						
					}
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(takePhotoIntent, TAKE_PHOTO_RQUEST);
					break;
				case 1: //Take Video
					Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					mMediaUri = getoutputMediaFileUri(MEDIA_TYPE_VIDEO);
					if(mMediaUri==null){
						//display an error
						Toast.makeText(MainActivity.this,"there was a problem acceccing your device's external storage", Toast.LENGTH_LONG).show();
						
					}else{
						videoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
						videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
						videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
						startActivityForResult(videoIntent,TAKE_VIDEO_RQUEST);
					}
					break;
				case 2: //Choose picture
					Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
					choosePhotoIntent.setType("image/*");
					startActivityForResult(choosePhotoIntent,PICK_PHOTO_RQUEST);
					break;
				case 3: //Choose video
					Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
					chooseVideoIntent.setType("Video/*");
					Toast.makeText(MainActivity.this,R.string.Video_file_size_warning,Toast.LENGTH_LONG);
					startActivityForResult(chooseVideoIntent,PICK_VIDEO_RQUEST);
				break;
				
				
			}
			
		}

		private Uri getoutputMediaFileUri(int mediaType) {
			
			if(isExternalStorageAvialabel()){
				//get the URI
				
				//1.get the external storage directory
				String appName = MainActivity.this.getString(R.string.app_name);
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);
				
				//2.create our sub directory
				if(!mediaStorageDir.exists()){
				if	(!mediaStorageDir.mkdirs())
				{
					Log.e(TAG,"Faild to create directory.");
					return null;
					
				}
				}
				//3.create a file name
				//4.create the file
				File mediaFile;
				Date now = new Date();
				String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);
				
				String path = mediaStorageDir.getPath()+File.separator;
				if(mediaType == MEDIA_TYPE_IMAGE){
					mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
				}else if(mediaType == MEDIA_TYPE_VIDEO ){
					mediaFile = new File(path + "VID_" + timestamp + ".mp4");
				}else{
					return null;
				}
					Log.d(TAG,"File:" + Uri.fromFile(mediaFile));
				
				//5.Return the file's Uri
			return Uri.fromFile(mediaFile);
			}else{
			return null;
			}
		}
		
		private boolean isExternalStorageAvialabel(){
			String state = Environment.getExternalStorageState();
			
			if(state.equals(Environment.MEDIA_MOUNTED)){
				return true;
			}else{
				return false;
			}
		}	
		};

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        
        ParseAnalytics.trackAppOpened(getIntent());
        
        ParseUser currentUser = ParseUser.getCurrentUser();
        
        if(currentUser == null){
        navigateToLogin();
        }else{
        	Log.i(TAG,currentUser.getUsername());
        }
        

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
    		//add it to the gallary
    		
    		if(requestCode == PICK_PHOTO_RQUEST || requestCode == PICK_VIDEO_RQUEST){
    			if(data == null){
    				Toast.makeText(this,getString(R.string.general_error),Toast.LENGTH_LONG).show();
    			}
    			else{
    				mMediaUri = data.getData();
    				
    			}
    			Log.i(TAG,"Media Uri: "+ mMediaUri);
    			if(requestCode == PICK_VIDEO_RQUEST){
    				//MAKE SURE THE FILE IS LESS THAN 10MB
    				int fileSize = 0;
    				InputStream inputStream = null;
    				
    				try{
    				 inputStream = getContentResolver().openInputStream(mMediaUri);
    				fileSize = inputStream.available();
    				}
    				catch(FileNotFoundException e){
        				Toast.makeText(this,R.string.error_opening_file,Toast.LENGTH_LONG).show();
        				return;
    					
    				}
    				catch(IOException e){
        				Toast.makeText(this,R.string.error_opening_file,Toast.LENGTH_LONG).show();
        				return;
    				}
    				finally{
    					try {
							inputStream.close();
						} catch (IOException e) {
							//Intentionally blank
						}
    					if(fileSize >= FILE_SIZE_LIMIT){
    						Toast.makeText(this,R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
    						return;
    					}
    				}
    			}
    		}else {
    		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    		mediaScanIntent.setData(mMediaUri);
    		sendBroadcast(mediaScanIntent);
    		}
    		
    		Intent recipientsIntent = new Intent(this,RecipientsActivity.class); 
    		recipientsIntent.setData(mMediaUri);
    		
    		String fileType;
    		if(requestCode == PICK_PHOTO_RQUEST ||requestCode == PICK_PHOTO_RQUEST){
    			fileType = ParseConstants.TYPE_IMAGE;
    		}else{
    			fileType = ParseConstants.TYPE_VIDEO;
    		}
    		
    		recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
    		startActivity(recipientsIntent);
    	}
    	else if(resultCode != RESULT_CANCELED){
    		Toast.makeText(this,R.string.general_error,Toast.LENGTH_LONG).show();
    	}
    	
    }


	private void navigateToLogin() {
		Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
     
   

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();
        
        switch(itemId){
        case R.id.action_logout: 
        	ParseUser.logOut();
        	 navigateToLogin();
       
        case R.id.action_edit_friends:
        		Intent intent = new Intent(this,EditFriendsActivity.class);
        		startActivity(intent);
        case R.id.action_camera:
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        	builder.setItems(R.array.camera_choices,  mDialogListner);
        	AlertDialog dialog = builder.create();
        	dialog.show();
        	
        
        
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

}
