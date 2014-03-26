package natemobile.apps.gridimagesearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.loopj.android.image.SmartImageView;

/**
 * ImageDisplayActivity
 * 
 * Activity for showing an image in Hi-res or full screen.
 * 
 * Using ShareActionProvider to provide sharing content functionality.
 * 
 * @author nkemavaha
 *
 */
public class ImageDisplayActivity extends Activity {

	/** Reference to image result when retrieving data*/
	private ImageResult imageResult;
	
	/** Reference to imageview of the image in high res/fullscreen*/
	private SmartImageView ivImage;
	
	private ShareActionProvider miShareAction;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_display);
		
		// Retrieve data from previous activity
		Intent i = getIntent();
		
		ivImage = (SmartImageView) findViewById( R.id.ivResult );
		imageResult = (ImageResult) i.getSerializableExtra("result");
		ivImage.setImageUrl( imageResult.getFullUrl() );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// NOTE: This is necessary for setup a share button on the ActionBar
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_display, menu);
		
		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem( R.id.miShare );
		
		// Fetch and store ShareActionProvider
		miShareAction = (ShareActionProvider) item.getActionProvider();
		setupShareAction();
		
		return true;
	}
	
	/**
	 * Setup share Action when clicking share button
	 * Reference & credits: https://github.com/thecodepath/android_guides/wiki/Sharing-Content-with-Intents
	 */
	public void setupShareAction() {
		// Fetch Bitmap Uri locally
		Uri bmpUri = getLocalBitmapUri( ivImage ); // see previous section

		// Create share intent as described above
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
		shareIntent.setType("image/*");

		// Attach share event to the menu item provider
		miShareAction.setShareIntent(shareIntent);

	}
	
	/**
	 * Helper function to get local bitmap URI data from the given ImageView object.
	 * Reference & credits: https://github.com/thecodepath/android_guides/wiki/Sharing-Content-with-Intents
	 * @param imageView
	 * @return	Uri object reference to image shown in ImageView object. Otherwise, NULL is returned.
	 */
	public Uri getLocalBitmapUri(ImageView imageView) {
		Uri bmpUri = null;
		Bitmap bitmap = null;
		
		try {
			// Grab BitmapData of the image from ImageView object
			bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		} catch (ClassCastException e) {
			// Exception error for casting -- this happens fron now and there.
			Log.d("DEBUG", "ERROR THROWN:: " + e.getMessage() );
		}
		
		if ( bitmap != null ) {

			// Write image to default external storage directory   
			try {
				File file =  new File(Environment.getExternalStoragePublicDirectory(  
						Environment.DIRECTORY_DOWNLOADS), "share_image.png");  
				FileOutputStream out = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();
				bmpUri = Uri.fromFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return bmpUri;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

}
