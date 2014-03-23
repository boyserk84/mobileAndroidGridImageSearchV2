package natemobile.apps.gridimagesearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.image.SmartImageView;

/**
 * ImageDisplayActivity
 * 
 * Activity for showing an image in Hi-res or full screen.
 * @author nkemavaha
 *
 */
public class ImageDisplayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_display);
		// Retreive data from previous activity
		Intent i = getIntent();
		ImageResult imageResult = (ImageResult) i.getSerializableExtra("result");
		SmartImageView ivImage = (SmartImageView) findViewById( R.id.ivResult );
		ivImage.setImageUrl( imageResult.getFullUrl() );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_display, menu);
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
