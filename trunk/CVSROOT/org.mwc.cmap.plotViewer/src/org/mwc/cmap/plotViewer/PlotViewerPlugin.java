package org.mwc.cmap.plotViewer;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.mwc.cmap.plotViewer.editors.chart.*;
import org.osgi.framework.BundleContext;

import MWC.GUI.Chart.Painters.SpatialRasterPainter;
import MWC.GenericData.WorldLocation;

import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class PlotViewerPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static PlotViewerPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/** fixed string used to indicate a string is in our location format
	 * 
	 */
	public static final String LOCATION_STRING_IDENTIFIER = "LOC:";
	
	/**
	 * The constructor.
	 */
	public PlotViewerPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// override the spatial raster painter - since we're working with SWT images, not JAva ones
		SpatialRasterPainter.overridePainter(new SWTRasterPainter());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PlotViewerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PlotViewerPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.mwc.cmap.plotViewer.PlotViewerPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/** convenience method to assist in extracting a location from the clipboard
	 * 
	 * @param txt
	 * @return
	 */
	public static WorldLocation fromClipboard(String txt)
	{		
		// get rid of the title
		String dataPart = txt.substring(PlotViewerPlugin.LOCATION_STRING_IDENTIFIER.length(), txt.length());
	  StringTokenizer st = new StringTokenizer(dataPart);
	  String latP = st.nextToken(",");
	  String longP = st.nextToken(",");
	  String depthP = st.nextToken();
	  Double _lat = new Double(latP);
	  Double _long = new Double(longP);
	  Double _depth = new Double(depthP);
	  WorldLocation res = new WorldLocation(_lat.doubleValue(), _long.doubleValue(), _depth.doubleValue());
		return res;
	}

	/** convenience method to check if a string is in our format
	 * 
	 * @param txt
	 * @return
	 */
	public static boolean isLocation(String txt)
	{
		return txt.startsWith(PlotViewerPlugin.LOCATION_STRING_IDENTIFIER);
	}

	/** convenience method to assist placing locations on the clipboard
	 * 
	 * @param loc
	 * @return
	 */
	public static String toClipboard(WorldLocation loc)
	{
		String res = PlotViewerPlugin.LOCATION_STRING_IDENTIFIER + loc.getLat() + "," + loc.getLong() + "," + loc.getDepth();
		return res;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.mwc.cmap.plotViewer", path);
	}
}
