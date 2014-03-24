package natemobile.apps.gridimagesearch;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug.IntToString;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * SearchActivity Class
 * Controller for main app -- search image
 * @author nkemavaha
 *
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
	
	/////////////////////
	/// Data fields
	/////////////////////
	
	/** List of Image result objects */
	ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
	
	/** Image Adapter for binding image result objects to view */
	ImageResultArrayAdapter imageAdapter;
	
	/** Image filter data */
	ImageFilterSettings imageFilterSettings = null;
	
	private boolean shouldClearResult = true;
	
	private int startIndex = 4;
	
	private String searchQuery = "";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		setupViews();
		
		// Setup adapter
		imageAdapter = new ImageResultArrayAdapter( this , imageResults );
		gvResults.setAdapter( imageAdapter );
		
		Log.d("DEBUG", "StartOnCreate -----------------------------");
		
		// Click on item and open full screen activity
		gvResults.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View parent, int position, long arg3) {
				Intent i = new Intent( getApplicationContext(), ImageDisplayActivity.class );
				ImageResult imageResult = imageResults.get( position );
				i.putExtra( "result" , imageResult );
				startActivity(i);
			}
		});
		
		// Setup customized onScroll listener (Endless scroll)
		gvResults.setOnScrollListener( new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				// Making sure it's not being loaded before search query is entered
				if ( searchQuery.equals("") == false ) {
					Log.d("DEBUG", "OnLoadMore:" + page + ": Total items count: " + totalItemsCount);
					startIndex = page*totalItemsCount;
					shouldClearResult = false;

					requestImageSearchByObject(imageFilterSettings);
				}
				
			}
		});
	}
	
	/** Setup view elements */
	private void setupViews() {
		etQuery = (EditText) findViewById(R.id.etQuery );
		gvResults = (GridView) findViewById(R.id.gvResults );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

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
	 * Callback when search button is clicked.
	 * @param v
	 */
	public void onImageSearch(View v) {
		String query = etQuery.getText().toString();
		searchQuery = query;
		
		resetValues();	// reset values
				
		Toast.makeText( this, "Searching for " + query, Toast.LENGTH_SHORT).show();
		
		// If there is no customized filter setting, using default one
		if ( imageFilterSettings == null ) {
			Log.d("DEBUG", "Default Filter Settings");
		} else {
			Log.d("DEBUG", "Customized Filter Settings");
		}
		
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
	
	private void resetValues() {
		// reset all values
		startIndex = 4;

		// Update flag so that we can have a new fresh result
		shouldClearResult = true;
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
		// Query Google Image Search API
		client.get(
				getImageQueryURLString(query, size, color, type, site),
				new JsonHttpResponseHandler() {

					// Upon success query
					@Override
					public void onSuccess(JSONObject response) {
						JSONArray imageJsonResults = null;
						JSONObject imageCursorResults = null;
						try {
							
							// Get data coming back from API response
							imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
							imageCursorResults = response.getJSONObject("responseData").getJSONObject("cursor");
							//Log.d("DEBUG", "Total Result " + imageCursorResults.getString("estimatedResultCount"));
							//Log.d("DEBUG", "Current Page Index is " + imageCursorResults.getString("currentPageIndex"));
							
							Log.d("DEBUG", "onSuccess getting response from Google! Total results: " + imageCursorResults.getString("estimatedResultCount"));
							updateImageAdapter( imageJsonResults );

						} catch (JSONException e) {
							e.printStackTrace();
							Log.d("DEBUG", "JSONException Error" + e.getMessage() + e.toString());
						}
					}
					

				});	
	}
	
	/**
	 * Request image search result by the given ImageFilterSettings object
	 * @param obj
	 */
	private void requestImageSearchByObject( ImageFilterSettings obj ) {
		String query = etQuery.getText().toString();
		searchQuery = query;
		Log.d("DEBUG", "requestImageSearchByOject:: " + searchQuery);
		if ( obj != null ) {
			requestImageSearch( query, obj.getFilterSize(), obj.getFilterColor(), obj.getFilterType(), obj.getFilterDomain() );
		} else {
			requestImageSearch( query, null, null, null, null );
		}
	}
	
	/**
	 * Update image adapter with the given JSONArray result from GOOGLE response
	 * @param imageJsonResults
	 */
	private void updateImageAdapter( JSONArray imageJsonResults) {
		if ( shouldClearResult == true ) {
			Log.d("DEBUG", "Clear ImageAdapter");
			imageResults.clear();	// clear result
			imageAdapter.addAll( ImageResult.fromJSONArray(imageJsonResults) ); // parse result and update adapter
			// TODO: Need to handle what if image not showing up
		} else {
			Log.d("DEBUG", "Add more to imageAdapter");
			imageResults.addAll(ImageResult.fromJSONArray(imageJsonResults));	
			imageAdapter.notifyDataSetInvalidated();
		}
		
		
		//Log.d("DEBUG", imageResults.toString() );
		//Log.d("DEBUG", "total " + imageResults.size());
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
		Log.d("DEBUG", "URL Query is " + result + " and startIndex is at " + startIndex );
		return result;
	}

}
