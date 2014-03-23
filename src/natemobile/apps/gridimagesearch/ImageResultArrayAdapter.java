package natemobile.apps.gridimagesearch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.loopj.android.image.SmartImageView;

/**
 * ImageResultArrayAdapter
 * 
 * Adapter for binding an array of ImageResult data to View
 * @author nkemavaha
 *
 */
public class ImageResultArrayAdapter extends ArrayAdapter<ImageResult> {

	/**
	 * Constructor
	 * @param context
	 * @param images
	 */
	public ImageResultArrayAdapter(Context context, ArrayList<ImageResult> images) {
		super(	
				context,		// context 
				R.layout.item_image_result,	// layout we'd like to use 
				images	// Result data that we'd like to bind to 
		);
	}

	/** Translation steps: Convert data to view */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageResult imageInfo = this.getItem( position );
		SmartImageView ivImage;
		
		if ( convertView == null ) {
			// if convertView doesn't exist or initialization
			LayoutInflater inflator = LayoutInflater.from( getContext() );
			ivImage = (SmartImageView) inflator.inflate(R.layout.item_image_result, parent, false);
		} else {
			// if convertView already exists, reusing the view object
			ivImage = (SmartImageView) convertView;
			ivImage.setImageResource(android.R.color.transparent ); // Reset image
			ivImage.setImageUrl( imageInfo.getThumbUrl() );	// set a new image
		}
		
		return ivImage;
	}
}
