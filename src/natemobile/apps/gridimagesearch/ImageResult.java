package natemobile.apps.gridimagesearch;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageResult Class
 * 
 * Strong-typed data for image result.
 * 
 * @author nkemavaha
 *
 */
public class ImageResult implements Serializable {

	private static final long serialVersionUID = 6423463272037319114L;
	/** URL references to full-screen/Hi-res version of the image */
	private String fullUrl;
	
	/** URL references to thumbnail version of the image*/
	private String thumbUrl;

	/**
	 * Constructor
	 * @param json
	 */
	public ImageResult(JSONObject json ) {
		try {
			fullUrl = json.getString("url");
			thumbUrl = json.getString("tbUrl");
		} catch (JSONException e) {
			fullUrl = null;
			thumbUrl = null;
		}
	}
	
	/**
	 * @return URL to full screen image
	 */
	public String getFullUrl() {
		return fullUrl;
	}

	/**
	 * @return URL to thumbnail image
	 */
	public String getThumbUrl() {
		return thumbUrl;
	}
	
	public String toString() {
		return thumbUrl;	
	}

	/**
	 * Parse JSON array to ArrayList<ImageResult>
	 * @param array
	 * @return
	 */
	public static ArrayList<ImageResult> fromJSONArray(
			JSONArray array) {
		
		ArrayList<ImageResult> results = new ArrayList<ImageResult>();
		
		for (int i = 0; i < array.length(); ++i ) {
			try {
				results.add( new ImageResult( array.getJSONObject( i ) ) );
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return results;
	}
}
