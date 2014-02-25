package com.huluyige.android.issyspots;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.esri.android.map.Callout;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;

public class MainActivity extends SherlockActivity implements
		ActionBar.OnNavigationListener {

	private MapView mapview;
	private ArcGISTiledMapServiceLayer basemapStreet;
	private ArcGISTiledMapServiceLayer basemapImagery;
	private ArcGISFeatureLayer featureLayer;
	private Callout callout;
	private ViewGroup calloutContent;
	private Boolean isMapViewLoaded = false;
	private Graphic identifiedGraphic;
	private int calloutStyle;
	private String[] types;
	private Menu optionsMenu;
	private int typeIndex=0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().show();
		setContentView(R.layout.main);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Retrieve the map and initial extent from XML layout
		mapview = (MapView) findViewById(R.id.map);

		mapview.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				// Check to see if map has successfully loaded
				if ((source == mapview) && (status == STATUS.INITIALIZED)) {
					isMapViewLoaded = true;
				} else if ((source == featureLayer)
						&& (status == STATUS.LAYER_LOADED)) {

					System.out.println("LAYER_LOADED "
							+ featureLayer.getTypes());
					FeatureType[] typesArray = featureLayer.getTypes();

					types = new String[typesArray.length + 1];
					for (int i = 0; i < types.length; i++) {
						if (i == 0) {
							types[i] = "Tous les types";
						} else {
							types[i] = typesArray[i - 1].getId();
						}
					}

					Context context = getSupportActionBar().getThemedContext();
					ArrayAdapter<String> list = new ArrayAdapter<String>(
							context, R.layout.sherlock_spinner_item, types);
					list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

					getSupportActionBar().setNavigationMode(
							ActionBar.NAVIGATION_MODE_LIST);
					getSupportActionBar().setListNavigationCallbacks(list,
							MainActivity.this);

				} else if ((source == basemapStreet)
						&& (status == STATUS.LAYER_LOADED)) {

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
				// if (isMapViewLoaded) {
				// Toast.makeText(MainActivity.this, "map clicked",
				// Toast.LENGTH_SHORT).show();
				// }
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

		Envelope initExtent = new Envelope(249854.9485678413,
				6244045.2504612515, 254278.4510396169, 6248320.524498849);
		mapview.setExtent(initExtent);

		
		ArcGISFeatureLayer.Options fOptions= new Options();
		fOptions.mode=ArcGISFeatureLayer.MODE.SNAPSHOT;
		String[] outFields ={"nom", "categorie", "adresse", "cp","ville","tel","web"};
		fOptions.outFields=outFields;
		featureLayer = new ArcGISFeatureLayer(this.getResources().getString(
				R.string.ISSY_FEATURE_LAYER), fOptions);
		featureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

			@Override
			public void onStatusChanged(Object arg0, STATUS arg1) {
				if (arg1 == STATUS.INITIALIZATION_FAILED) {
					Toast.makeText(MainActivity.this,
							"Échec de l'initialisation", Toast.LENGTH_SHORT)
							.show();
				} else if (arg1 == STATUS.LAYER_LOADING_FAILED) {
					Toast.makeText(MainActivity.this,
							"Échec du chargement de couche", Toast.LENGTH_SHORT)
							.show();
				}				

			}
		});

		mapview.addLayer(featureLayer);
		// attribute ESRI logo to map
		mapview.setEsriLogoVisible(true);
		// enable map to wrap around date line

		mapview.enableWrapAround(true);

		calloutStyle = R.xml.identify_calloutstyle;
		LayoutInflater inflater = getLayoutInflater();
		callout = mapview.getCallout();
		calloutContent = (ViewGroup) inflater.inflate(R.layout.callout_content,
				null);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add("Imagerie").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add("List").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		optionsMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		if (item.getTitle().equals("Imagerie")) {
			basemapStreet.setVisible(false);
			basemapImagery.setVisible(true);
			item.setTitle("Carte");
			return true;
		}
		if (item.getTitle().equals("Carte")) {
			basemapStreet.setVisible(true);
			basemapImagery.setVisible(false);
			item.setTitle("Imagerie");
			return true;
		} else if (item.getTitle().equals("List")) {
			Intent intent= new Intent(MainActivity.this, ListActivity.class);
			String type=types[this.typeIndex];
			intent.putExtra("type", type);
			intent.putExtra("url", featureLayer.getUrl());
			startActivity(intent);
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {

		for (String type : types) {
			menu.add(type);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		// Note how this callback is using the fully-qualified class name
		Toast.makeText(this, "Got click: " + item.toString(),
				Toast.LENGTH_SHORT).show();
		return true;
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

	private void ShowCallout(Callout calloutView, Graphic graphic,
			Point mapPoint) {

		// Get the values of attributes for the Graphic
		String cityName = (String) graphic.getAttributeValue("nom");
		String countryName = (String) graphic.getAttributeValue("categorie");
		// String cityPopulationValue = ((Double)
		// graphic.getAttributeValue("POPULATION")).toString();

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
		// StringBuilder cityPopulation = new StringBuilder();
		// cityPopulation.append("Population: ");
		// cityPopulation.append(cityPopulationValue);
		//
		// TextView calloutTextLine2 = (TextView) findViewById(R.id.population);
		// calloutTextLine2.setText(cityPopulation);
		calloutView.setContent(calloutContent);
		calloutView.show();
		// Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
	}

	public void callout_onClick(View view) {

		Intent intent = new Intent(MainActivity.this, DetailActivity.class);
		intent.putExtra("data", identifiedGraphic);
		startActivity(intent);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		System.out.println("onNavigationItemSelected: " + itemPosition);

		if (itemPosition != 0) {
			featureLayer
					.setDefinitionExpression(getRightWhereClause(types[itemPosition]));
			featureLayer.refresh();
			System.out.println("features: "+featureLayer.getGraphicIDs().length);
		} else {
			featureLayer.setDefinitionExpression(featureLayer
					.getDefaultDefinitionExpression());
			featureLayer.refresh();
		}
		typeIndex=itemPosition;
		return true;
	}

	public static String getRightWhereClause(String str) {
		if (str.contains("\'")) {
			String newSt = str.replace("\'", "\'\'");
			return "categorie like " + "\'" + newSt + "\'";
		} else
			return "categorie like " + "\'" + str + "\'";
	}

	public FeatureSet createWindTurbinesFeatureSet(String path) {
		FeatureSet fs = null;

		try {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory
					.createJsonParser(getAssets().open(path));
			parser.nextToken();
			fs = FeatureSet.fromJson(parser);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fs;
	}

	public String assetFileToString(String path) throws IOException {
		InputStream stream = getAssets().open(path);
		int size = stream.available();
		byte[] buffer = new byte[size];
		stream.read(buffer);
		stream.close();
		String text = new String(buffer);
		return text;
	}
}