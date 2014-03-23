package natemobile.apps.gridimagesearch;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

	public static final String GOOGLE_MAIN_REQUEST_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		setupViews();
		
		// Setup adapter
		imageAdapter = new ImageResultArrayAdapter( this , imageResults );
		gvResults.setAdapter( imageAdapter );
		
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
		
		Toast.makeText( this, "Searching for " + query, Toast.LENGTH_SHORT).show();
		
		// TODO:
		requestImageSearch( query, "","","","" );
		
	}
	
	/**
	 * 
	 * @param query
	 * @param size
	 * @param color
	 * @param type
	 * @param site
	 */
	private void requestImageSearch(String query, String size, String color, String type, String site) {
		// Send ASYNC HTTP client Request
		AsyncHttpClient client = new AsyncHttpClient();
		// Query Google Image Search API
		client.get(
				getImageQueryURLString(query),
				new JsonHttpResponseHandler() {

					// Upon success query
					@Override
					public void onSuccess(JSONObject response) {
						JSONArray imageJsonResults = null;
						try {
							// Get data coming back from API response
							imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");

							// TODO: Note probably need to implement callback to separate this logic from view
							imageResults.clear();	// clear result
							imageAdapter.addAll( ImageResult.fromJSONArray(imageJsonResults)); // parse result


							Log.d("DEBUG", imageResults.toString() );
							Log.d("DEBUG", "total " + imageResults.size());

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					// TODO: implement on failure

				});	
	}
	
	private void requestImageSearchByObject( ImageFilterSettings obj ) {
		String query = etQuery.getText().toString();
		requestImageSearch( query, obj.getFilterSize(), obj.getFilterColor(), obj.getFilterType(), obj.getFilterDomain() );
	}
	
	/**
	 * Callback when filter setting icon on the action bar is pressed.
	 * @param mi
	 */
	public void onFilterSettingsPress(MenuItem mi) {
		Intent i = new Intent( this, ImageFilterActivity.class );
		startActivityForResult( i , REQUEST_FILTER_CODE );	
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( requestCode == REQUEST_FILTER_CODE && resultCode == RESULT_OK ) {
			ImageFilterSettings filterData = (ImageFilterSettings) data.getSerializableExtra("result");
			
			// TODO: Remove this once done
			if ( filterData == null ) {
				Log.d("DEBUG", "shit");
			} else {
				Log.d("DEBUG", filterData.toString());	
			}
			
			requestImageSearchByObject( filterData );
		}
	}
	
	////////////////////////////////////////////////////////////
	// Helper function for sending query to Google Image
	///////////////////////////////////////////////////////////
	
	/**
	 * Get an image query URL based on query String
	 * @param query
	 * @return
	 */
	public String getImageQueryURLString( String query ) {
		String result = GOOGLE_MAIN_REQUEST_URL + Uri.encode(query);
		return result;	
	}
	
	/**
	 * Get an image query URL based on the given criteria and query string
	 * @param query
	 * @param size
	 * @param color
	 * @param type
	 * @param site
	 * @return
	 */
	public String getImageQueryURLString(String query, String size, String color, String type, String site ) {
		String filterSize = GOOGLE_FILTER_IMG_SIZE + Uri.encode( size );
		String filterColor =  GOOGLE_FILTER_IMG_COLOR+ Uri.encode( color );
		String filterType =  GOOGLE_FILTER_IMG_TYPE+ Uri.encode( type );
		String filterSite = GOOLGE_FILTER_IMG_DOMAIN + Uri.encode( site );
		
		String result = getImageQueryURLString(query) + filterSize + filterColor + filterType + filterSite;
		return result;
	}

}
