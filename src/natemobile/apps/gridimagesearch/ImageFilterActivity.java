package natemobile.apps.gridimagesearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * ImageFilterActivity
 * 
 * Activity for users to configurate image filter search values.
 * @author nkemavaha
 *
 */
public class ImageFilterActivity extends Activity {

	Spinner spFilterSize;
	
	Spinner spFilterType;
	
	Spinner spFilterColor;
	
	MenuItem miSaveSettings;
	
	
	private String filterSize;
	
	private String filterType;
	
	private String filterDomain;
	
	private String filterColor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_filter);
		setupViews();
	}
	
	/** Setup views */
	private void setupViews() {
		spFilterSize = (Spinner) findViewById(R.id.spFilterSize);
		spFilterType = (Spinner) findViewById(R.id.spFilterType);
		spFilterColor = (Spinner) findViewById(R.id.spFilterColor);
		
		setupSpinner( spFilterSize, R.array.filterSize_array );
		setupSpinner( spFilterType, R.array.filterType_array );
		setupSpinner( spFilterColor, R.array.filterColor_array );
	}
	
	/**
	 * Helper function to setup spinner and populate it with predefined string value
	 * @param spinner
	 * @param enumStringArray
	 */
	private void setupSpinner(Spinner spinner, int enumStringArray) {
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				enumStringArray, android.R.layout.simple_spinner_item);
		
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);	
		
	}
	
	private void updateFilterValuesFromInput() {
		filterSize = spFilterSize.getSelectedItem().toString();
		filterType = spFilterType.getSelectedItem().toString();
		filterColor = spFilterColor.getSelectedItem().toString();
	}
	
	/**
	 * Callback when users click the filter setting button on the actionbar
	 * @param mi
	 */
	public void onSaveFilterSettingsPress(MenuItem mi ) {
		updateFilterValuesFromInput();
		ImageFilterSettings newSettings = new ImageFilterSettings( filterSize, filterColor, filterType, filterDomain  );
		
		Intent data = new Intent();
		data.putExtra("result", newSettings);
		setResult(RESULT_OK, data);
		
		finish();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_filter, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

}
