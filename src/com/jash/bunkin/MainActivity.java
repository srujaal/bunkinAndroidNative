package com.jash.bunkin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
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
import android.widget.Toast;

import com.jash.bunkin.Adapters.SectionsPagerAdapter;
import com.jash.bunkin.actions.ComposeActivity;
import com.jash.bunkin.actions.CreateBunkin;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	public static final String TAG = MainActivity.class.getSimpleName();
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int TAKE_VIDEO_REQUEST = 1;
	public static final int PICK_PHOTO_REQUEST = 2;
	public static final int PICK_VIDEO_REQUEST = 3;

	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	
	public static final int FILE_SIZE_LIMIT = 1024*1024*10; //10MB

	protected Uri mMediaUri;
	
	protected DialogInterface.OnClickListener mStatusDialogListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent intent;
			switch(which) {
			case 0:
				intent = new Intent(MainActivity.this, ComposeActivity.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(MainActivity.this, CreateBunkin.class);
				startActivity(intent);
				break;
			}
			
		}
	};

	protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {			
			switch (which) {
			case 0:
				// Take Picture
				Intent takePhotointent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
				if (mMediaUri == null) {
					Toast.makeText(MainActivity.this,
							R.string.error_external_storage, Toast.LENGTH_LONG)
							.show();
				} else {
					takePhotointent
							.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(takePhotointent, TAKE_PHOTO_REQUEST);
				}
				break;
			case 1:
				// Take Video
				Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
				if (mMediaUri == null) {
					Toast.makeText(MainActivity.this,
							R.string.error_external_storage, Toast.LENGTH_LONG)
							.show();
				} else {
					videoIntent
							.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri)
							.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
							.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
				}
				break;
			case 2:
				// Choose Picture
				Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				choosePhotoIntent.setType("image/*");
				startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
				break;
			case 3:
				// Choose Video
				Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				chooseVideoIntent.setType("video/*");
				Toast.makeText(MainActivity.this, R.string.vide_file_size_warning, Toast.LENGTH_LONG).show();
				startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);				
				break;
			}
		}

		private Uri getOutputMediaFileUri(int mediaType) {
			// To be safe, you should check that the SDCard is mounted
			// using Environment.getExternalStorageState() before doing this.
			if (isExternalStorageAvailable()) {
				// 1. Get the external storage directory
				String appName = MainActivity.this.getString(R.string.app_name);
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);

				// 2. Create our subdirectory
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Log.e(TAG, "failed to create directory");
						return null;
					}
				}

				// 3. Create a file
				File mediafile;
				Date now = new Date();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
						Locale.US).format(now);
				String path = mediaStorageDir.getPath() + File.separator;
				if(mediaType == MEDIA_TYPE_IMAGE){
					mediafile = new File(path+"IMG_"+timeStamp+".jpg");					
				} else if(mediaType == MEDIA_TYPE_VIDEO){
					mediafile = new File(path+"IMG_"+timeStamp+".mp4");	
				} else { return null; }
				
				Log.d(TAG,"File:"+Uri.fromFile(mediafile));
				
				//4. Return the URI
				return Uri.fromFile(mediafile);
				
			} else {
				return null;
			}
		}

		private boolean isExternalStorageAvailable() {
			String state = Environment.getExternalStorageState();

			if (state.equals(Environment.MEDIA_MOUNTED)) {
				return true;
			} else {
				return false;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ParseAnalytics.trackAppOpened(getIntent());
		ParseUser currentUser = ParseUser.getCurrentUser();

		if (currentUser == null) {
			navigateToLogin();
		} else {
			Log.i(TAG, currentUser.getUsername());
		}
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.

		mSectionsPagerAdapter = new SectionsPagerAdapter(this,
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);		
		
        if (resultCode == RESULT_OK) {
            // Image captured and saved to fileUri specified in the Intent
        	
        	if(requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST){
        		if(data == null){
        			Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
        		} else{
        			mMediaUri = data.getData();
        		}
        		
        		if(requestCode == PICK_VIDEO_REQUEST) {
        			// make sure the file is less than 10mb        			
        			int fileSize = 0;
        			InputStream inputStream = null;
        			try{
	        			inputStream = getContentResolver().openInputStream(mMediaUri); 
	        			fileSize = inputStream.available();
        			}
        			catch (FileNotFoundException e){
        				Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
        				return;
        			}
        			catch (IOException e){
        				Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
        				return;
        			}
        			
        			finally{
        				try {
							inputStream.close();
						} catch (IOException e) { }
        			}
        			
        			if(fileSize >= FILE_SIZE_LIMIT){
        				Toast.makeText(this, R.string.file_size_warning, Toast.LENGTH_LONG).show();
        				return;
        			}
        		}
        	}
        	else{        	
	            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	            mediaScanIntent.setData(mMediaUri);
	            sendBroadcast(mediaScanIntent);
        	}
        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
        	Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
        	return;
        } else {
            // Image capture failed, advise user
        }
	    
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
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
		int id = item.getItemId();
		Intent intent;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case R.id.action_logout:
			ParseUser.logOut();
			navigateToLogin();
			return true;
		case R.id.action_friends:
			intent = new Intent(this, FriendsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_camera:			
			builder.setItems(R.array.camera_choices, mDialogListener);	
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		case R.id.action_compose:
			/*intent = new Intent(this, ComposeActivity.class);
			startActivity(intent);
			return true;*/			
			builder.setItems(R.array.status_choices, mStatusDialogListener);		
			AlertDialog statusDlg = builder.create();
			statusDlg.show();
			return true;
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

}
