package com.huluyige.android.issyspots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.esri.android.map.Callout;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;

public class MainActivity extends SherlockActivity {

	private MapView mapview;
	private ArcGISTiledMapServiceLayer basemapStreet;
	private ArcGISTiledMapServiceLayer basemapImagery;
	private ArcGISFeatureLayer featureLayer;
	private Callout callout;
	private ViewGroup calloutContent;
	private Boolean isMapViewLoaded=false;
	private Graphic identifiedGraphic;
	private int calloutStyle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().show();
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mapview = (MapView) findViewById(R.id.map);
		
		mapview.setOnStatusChangedListener(new OnStatusChangedListener() {
			 
		      private static final long serialVersionUID = 1L;
		 
		      @Override
		      public void onStatusChanged(Object source, STATUS status) {
		        // Check to see if map has successfully loaded
		        if ((source == mapview) && (status == STATUS.INITIALIZED)) {
		        	isMapViewLoaded = true;
		        }
		      }
		    });
		mapview.setOnSingleTapListener(new OnSingleTapListener() {
			 
		      private static final long serialVersionUID = 1L;
		 
		      @Override
		      public void onSingleTap(float x, float y) {
				if (isMapViewLoaded) {
					// If map is initialized and Single tap is registered on
					// screen
					// identify the location selected
					identifyLocation(x, y);
				}
//		        if (isMapViewLoaded) {
//		        	Toast.makeText(MainActivity.this, "map clicked", Toast.LENGTH_SHORT).show();
//		        }
		      }
		    });
		
		/* create an initial basemap */
		basemapStreet = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.WORLD_STREET_MAP));
		basemapImagery = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.WORLD_IMAGERY_MAP));
		// Add basemap to MapView
		mapview.addLayer(basemapStreet);
		mapview.addLayer(basemapImagery);
		basemapStreet.setVisible(true);
		basemapImagery.setVisible(false);

		featureLayer = new ArcGISFeatureLayer(
				"http://services.opengeodata.fr/arcgis/rest/services/ISSY_MLX/ISSY_ETABL_PUB/FeatureServer/0",
				ArcGISFeatureLayer.MODE.SNAPSHOT);
		mapview.addLayer(featureLayer);
		// attribute ESRI logo to map
		mapview.setEsriLogoVisible(true);
		// enable map to wrap around date line
		mapview.enableWrapAround(true);
		
		calloutStyle = R.xml.identify_calloutstyle;
		LayoutInflater inflater = getLayoutInflater();
		callout=mapview.getCallout();
		calloutContent=(ViewGroup) inflater.inflate(R.layout.callout_content, null);
		callout.setContent(calloutContent);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapview.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapview.unpause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.basemap_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.World_Street_Map:
			basemapStreet.setVisible(true);
			basemapImagery.setVisible(false);
			return true;
		case R.id.World_Imagery_Map:
			basemapStreet.setVisible(false);
			basemapImagery.setVisible(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private void identifyLocation(float x, float y) {

		// Hide the callout, if the callout from previous tap is still showing
		// on map
		if (callout.isShowing()) {
			callout.hide();
		}

		// Find out if the user tapped on a feature
		SearchForFeature(x, y);

		// If the user tapped on a feature, then display information regarding
		// the feature in the callout
		if (identifiedGraphic != null) {
			Point mapPoint = mapview.toMapPoint(x, y);
			// Show Callout
			ShowCallout(callout, identifiedGraphic, mapPoint);
		}
	}
	private void SearchForFeature(float x, float y) {
		 
	    Point mapPoint = mapview.toMapPoint(x, y);
	 
	    if (mapPoint != null) {
	 
	      for (Layer layer : mapview.getLayers()) {
	        if (layer == null)
	          continue;
	 
	        if (layer instanceof ArcGISFeatureLayer) {
	          ArcGISFeatureLayer fLayer = (ArcGISFeatureLayer) layer;
	          // Get the Graphic at location x,y
	          identifiedGraphic = GetFeature(fLayer, x, y);
	        } else
	          continue;
	      }
	    }
	  }
	 
	  private Graphic GetFeature(ArcGISFeatureLayer fLayer, float x, float y) {
	 
	    // Get the graphics near the Point.
	    int[] ids = fLayer.getGraphicIDs(x, y, 10, 1);
	    if (ids == null || ids.length == 0) {
	      return null;
	    }
	    Graphic g = fLayer.getGraphic(ids[0]);
	    return g;
	  }
	  private void ShowCallout(Callout calloutView, Graphic graphic, Point mapPoint) {
		  
		    // Get the values of attributes for the Graphic
		    String cityName = (String) graphic.getAttributeValue("nom");
		    String countryName = (String) graphic.getAttributeValue("categorie");
		   // String cityPopulationValue = ((Double) graphic.getAttributeValue("POPULATION")).toString();
		 
		    // Set callout properties
		    calloutView.setCoordinates(mapPoint);
		    calloutView.setStyle(calloutStyle);
		    calloutView.setMaxWidth(325);
		 
		    // Compose the string to display the results
		    StringBuilder cityCountryName = new StringBuilder();
		    cityCountryName.append(cityName);
		    cityCountryName.append(", ");
		    cityCountryName.append(countryName);
		 
		    TextView calloutTextLine1 = (TextView) findViewById(R.id.calloutName);
		    calloutTextLine1.setText(cityCountryName);
		 
		    // Compose the string to display the results
//		    StringBuilder cityPopulation = new StringBuilder();
//		    cityPopulation.append("Population: ");
//		    cityPopulation.append(cityPopulationValue);
//		 
//		    TextView calloutTextLine2 = (TextView) findViewById(R.id.population);
//		    calloutTextLine2.setText(cityPopulation);
		    calloutView.setContent(calloutContent);
		    calloutView.show();
		 // Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
		  }
	  
	  public void callout_onClick(View view){
		  Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
	  }
}