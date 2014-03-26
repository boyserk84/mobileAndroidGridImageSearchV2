package natemobile.apps.gridimagesearch;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * SearchActivity Class
 * Controller for main app -- search image
 * @author nkemavaha
 */
public class SearchActivity extends Activity {

	public static final String GOOGLE_MAIN_REQUEST_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=";
	public static final String GOOGLE_REQUEST_START_INDEX = "&start=";
	public static final String GOOGLE_FILTER_IMG_SIZE = "&imgsz=";
	public static final String GOOGLE_FILTER_IMG_TYPE = "&imgtype=";
	public static final String GOOGLE_FILTER_IMG_COLOR = "&imgcolor=";
	public static final String GOOLGE_FILTER_IMG_DOMAIN = "&sitesearch=";
	
	public static final int REQUEST_FILTER_CODE = 99;
	
	////////////////////////
	/// UI elements
	///////////////////////
	
	/** Query text */
	private EditText etQuery;
	
	/** Image result (gridview)*/
	private GridView gvResults;
	
	/** Customized search or filter button */
	private Button btnSearchAndFilter;
	
	/////////////////////
	/// Data fields
	/////////////////////
	
	/** List of Image result objects */
	ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
	
	/** Image Adapter for binding image result objects to view */
	ImageResultArrayAdapter imageAdapter;
	
	/** Image filter data */
	ImageFilterSettings imageFilterSettings = null;
	
	/** Flag to indicate whether we should clear all result or not*/
	private boolean shouldClearResult = true;
	
	/** Starting index/offset for Goolge API */
	private int startIndex = 4;
	
	/** Search query term */
	private String searchQuery = "";
	
	/** Flag checking if last request is successful.*/
	private boolean isLastRequestSuccess = true;
	
	/** Flag indicate which mode user is in (search mode vs filter mode)*/
	private boolean isInSearchMode = true;
	
	///////////////////////////////////////////
	// Internal, only for debugging purpose
	///////////////////////////////////////////
	
	/** Flag determine if we need to show log activity */
	private boolean isOnProduction = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		setupViews();
		setupCustomActionBar();
		showLog("StartOnCreate -----------------------------");
		
		// Setup adapter
		imageAdapter = new ImageResultArrayAdapter( this , imageResults );
		gvResults.setAdapter( imageAdapter );
		
		// Setup customized onScroll listener (Endless scroll)
		gvResults.setOnScrollListener( new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				requestMoreImages(page);			
			}
		});
		
		// Click on item and open full screen activity
		gvResults.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View parent, int position, long arg3) {
				Intent i = new Intent( getApplicationContext(), ImageDisplayActivity.class );
				ImageResult imageResult = imageResults.get( position );
				i.putExtra( "result" , imageResult );
				i.putExtra( "resultList" , imageResults);
				i.putExtra( "startPosition", position);
				startActivity(i);
			}
		});
		
	}
	
	/**
	 * Helper function for showing debug log so that we can toggle on/off
	 * @param message
	 */
	private void showLog( String message ) {
		if ( isOnProduction == false ) {
			Log.d("DEBUG", message);
		}
	}
	
	/**
	 * Helper function to request more image data
	 * @param page
	 */
	private void requestMoreImages(int page) {
		if ( isLastRequestSuccess == true ) {
			startIndex = page * 8;
			showLog( "Request More Image with startIndex" + startIndex );
			shouldClearResult = false;	// make sure we append data instead of reset
			requestImageSearchByObject( imageFilterSettings );
		}
	}
	
	/** Setup view elements */
	private void setupViews() {
		etQuery = (EditText) findViewById(R.id.etQuery );
		gvResults = (GridView) findViewById(R.id.gvResults );
	}

	@Override
	public final boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Setup a custom action bar without using AcitonBarSherlock
	 * Credits to Jeff G.
	 */
	private void setupCustomActionBar() {
		ActionBar actionBar = getActionBar();
		
		actionBar.setCustomView(R.layout.custom_search_bar);
		
		btnSearchAndFilter = (Button) actionBar.getCustomView().findViewById(R.id.btnSearchAndFilter);
		etQuery = (EditText) actionBar.getCustomView().findViewById(R.id.etQuery);
		
		// This happens during run-time,therefore we can't just attach onClick() event in XML
		
		// Listen for button press on the action bar, depending on which mode we are in
		// If we are in a search mode then search the image
		// Otherwise, will open filter activity.
		btnSearchAndFilter.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( isInSearchMode ) {
					onImageSearch( v );
				} else {
					onFilterSettingsPress( null );
				}
			}
		});
		
		// Listen for text change so we can determine if we are in filter mode or search mode
		etQuery.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				setSearchMode( true );
			}
		});
		
		// Need this to make sure it uses the custom one.
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	}
	
	/**
	 * Helper function to update query input for user
	 */
	private void updateQueryInput() {
		String query = etQuery.getText().toString();
		searchQuery = query;	// Fetch a new query term	
	}
	
	/**
	 * Helper function to reset values for the next search term
	 */
	private void resetValues() {
		// reset all values
		startIndex = 0;

		// Update flag so that we can have a new fresh result
		shouldClearResult = true;
	}
	
	/**
	 * Update current image on the searchAndFilter button based on the current mode we are in
	 */
	private void updateCurrentModeButton() {
		if ( isInSearchMode ) {
			btnSearchAndFilter.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_search , 0, 0, 0);
		} else {
			btnSearchAndFilter.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_settings , 0, 0, 0);
		}
	}
	
	/**
	 * Set the current mode and update its visual on the button
	 * @param enable
	 */
	private void setSearchMode(boolean enable) {
		isInSearchMode = enable;
		updateCurrentModeButton();
	}
	
	/**
	 * Callback when search button is clicked.
	 * @param v
	 */
	public void onImageSearch(View v) {
		updateQueryInput();
		resetValues();	// reset values
				
		Toast.makeText( this, "Searching for " + searchQuery, Toast.LENGTH_SHORT).show();
				
		requestImageSearchByObject( imageFilterSettings );
		
	}
	
	/**
	 * Callback when filter setting icon on the action bar is pressed.
	 * @param mi
	 */
	public void onFilterSettingsPress(MenuItem mi) {
		Intent i = new Intent( this, ImageFilterActivity.class );
		startActivityForResult( i , REQUEST_FILTER_CODE );	
	}
	
	/**
	 * Callback when return to this activity from other activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( requestCode == REQUEST_FILTER_CODE && resultCode == RESULT_OK ) {
			
			// Retrieve settings data from ImageFilterActivity.
			ImageFilterSettings filterData = (ImageFilterSettings) data.getSerializableExtra("result");
			
			// Save this filter settings for subsequent used.
			imageFilterSettings = filterData;	
			
			resetValues();

			// Update the search result with a new filter settings
			requestImageSearchByObject( filterData );
			
			Toast.makeText( this, "Filters updated for query:" + searchQuery, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Request an image search result by filter values
	 * See https://developers.google.com/image-search/v1/jsondevguide#json_snippets_java for details
	 * @param query		String search query
	 * @param size		Image size
	 * @param color		Image color
	 * @param type		Image type
	 * @param site		Image specific domain (i.e. yahoo.com, photobucket.com)
	 */
	private void requestImageSearch(String query, String size, String color, String type, String site) {
		// Send ASYNC HTTP client Request
		AsyncHttpClient client = new AsyncHttpClient();
		
		isLastRequestSuccess = false;
		
		// Query Google Image Search API
		client.get(
				getImageQueryURLString(query, size, color, type, site),
				new JsonHttpResponseHandler() {

					// Upon success query
					@Override
					public void onSuccess(JSONObject response) {
						isLastRequestSuccess = true;
						JSONArray imageJsonResults = null;
						try {
							
							// Get data coming back from API response
							imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
							showLog( "OnSuccess getting response from Google!" );
							updateImageAdapter( imageJsonResults );

						} catch (JSONException e) {
							e.printStackTrace();
							showLog( "JSONException Error" + e.getMessage() + e.toString());
						}
					}
					

				});	
	}
	
	/**
	 * Request image search result by the given ImageFilterSettings object
	 * @param obj
	 */
	private void requestImageSearchByObject( ImageFilterSettings obj ) {
		updateQueryInput();
		if ( obj != null ) {
			requestImageSearch( searchQuery, obj.getFilterSize(), obj.getFilterColor(), obj.getFilterType(), obj.getFilterDomain() );
		} else {
			requestImageSearch( searchQuery, null, null, null, null );
		}
	}
	
	/**
	 * Update image adapter with the given JSONArray result from GOOGLE response
	 * @param imageJsonResults
	 */
	private void updateImageAdapter( JSONArray imageJsonResults) {
		
		if ( shouldClearResult == true ) {
			imageResults.clear();	// clear result
			imageAdapter.addAll( ImageResult.fromJSONArray(imageJsonResults) ); // parse result and update adapter
			imageAdapter.notifyDataSetChanged();
		} else {
			imageAdapter.addAll( ImageResult.fromJSONArray(imageJsonResults) );	// Update adapter
			imageAdapter.notifyDataSetChanged();
		}
		
		setSearchMode( false );	// Update current mode
	}	

	
	////////////////////////////////////////////////////////////
	// Helper function for sending query to Google Image
	///////////////////////////////////////////////////////////
	
	/**
	 * Get an image query URL based on query String
	 * @param query	Search string query
	 * @return Query URL string.
	 */
	public String getImageQueryURLString( String query ) {
		String result = GOOGLE_MAIN_REQUEST_URL + Uri.encode(query) + GOOGLE_REQUEST_START_INDEX + Integer.toString( startIndex );
		return result;	
	}
	
	/**
	 * Get an image query URL based on the given criteria and query string
	 * @param query		Search string query
	 * @param size		Image size
	 * @param color		Image color
	 * @param type		Image type
	 * @param site		Image specific domain
	 * @return Query URL string with appropriate parameters.
	 */
	public String getImageQueryURLString(String query, String size, String color, String type, String site ) {
		String filterSize = (size!=null)?GOOGLE_FILTER_IMG_SIZE + Uri.encode( size ):"";
		String filterColor =  (color!=null)?GOOGLE_FILTER_IMG_COLOR+ Uri.encode( color ):"";
		String filterType =  (type!=null)?GOOGLE_FILTER_IMG_TYPE+ Uri.encode( type ):"";
		String filterSite = (site!=null)?GOOLGE_FILTER_IMG_DOMAIN + Uri.encode( site ):"";
		
		String result = getImageQueryURLString(query) + filterSize + filterColor + filterType + filterSite;
		showLog( "URL Query is " + result  );
		return result;
	}

}
