package com.huluyige.android.issyspots;

import android.app.Activity;
import android.os.Bundle;


import com.esri.android.map.MapView;


public class MainActivity extends Activity {
	
	MapView mMapView ;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		//mMapView = new MapView(this);

    }

	@Override 
	protected void onDestroy() { 
		super.onDestroy();
 }
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
 }
	@Override 	protected void onResume() {
		super.onResume(); 
		mMapView.unpause();
	}

}