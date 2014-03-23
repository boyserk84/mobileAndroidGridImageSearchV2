package natemobile.apps.gridimagesearch;

import java.io.Serializable;

/**
 * ImageFilterSettings
 * 
 * Strong-typed data for image filter setting values
 * 
 * @author nkemavaha
 *
 */
public class ImageFilterSettings implements Serializable{

	private static final long serialVersionUID = 5501671489893612952L;
	
	private String filterSize ="";
	
	private String filterType = "";
	
	private String filterColor = "";
	
	private String filterDomain = "";
	
	/**
	 * Constructor
	 * @param size
	 * @param color
	 * @param type
	 * @param domain
	 */
	public ImageFilterSettings(String size, String color, String type, String domain ) {
		filterSize = size;
		filterType = type;
		filterColor = color;
		filterDomain = domain;
	}
	
	/**
	 * Constructor
	 * @param size
	 * @param color
	 * @param type
	 * @param domain
	 */
	public ImageFilterSettings(String size, String color, String type ) {
		filterSize = size;
		filterType = type;
		filterColor = color;
	}
	
	// TODO: Set default value if leave blank
	/**
	 * @return Image size value
	 */
	public String getFilterSize() {
		return filterSize;
	}

	/**
	 * @return Image type value
	 */
	public String getFilterType() {
		return filterType;
	}


	/** 
	 * @return Image predominant color value
	 */
	public String getFilterColor() {
		return filterColor;
	}

	/** 
	 * @return Image's domain (i.e. www.yahoo.com)
	 */
	public String getFilterDomain() {
		return filterDomain;
	}
	
	@Override
	public String toString() {
		return "Filter options:" + filterSize + ":" + filterColor + ":" + filterType + ":" + filterDomain;
		
	}




}
