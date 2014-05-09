package com.ensaitechnomobile.geoloc;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.http.IHttpClientFactory;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import com.ensai.appli.R;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class NewGeolocalisation extends ActionBarActivity implements
		LocationListener {

	private MapView mapView;
	private MapController mc;

	private LocationManager lm;

	private double latitude;
	private double longitude;
	private double altitude;
	private float accuracy;

	private SimpleLocationOverlay mMyLocationOverlay;
	private ScaleBarOverlay mScaleBarOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geoloc_osm);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		HttpClientFactory.setFactoryInstance(new IHttpClientFactory() {
			public HttpClient createHttpClient() {
				final DefaultHttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
						"ensaitechnomobiles.AMS");
				return client;
			}
		});

		mapView = (MapView) this.findViewById(R.id.mapview);
		mapView.setTileSource(TileSourceFactory.CYCLEMAP);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		mc = (MapController) this.mapView.getController();
		GeoPoint startPoint = new GeoPoint(48.13, -1.74098);
		mc.setCenter(startPoint);
		mc.setZoom(9);

	}

	@Override
	protected void onResume() {
		super.onResume();
		lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0,
					this);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0,
				this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		lm.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();
		accuracy = location.getAccuracy();

		GeoPoint p = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));
		mc.animateTo(p);
		mc.setCenter(p);
		mc.setZoom(17);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		String newStatus = "";
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			newStatus = "OUT_OF_SERVICE";
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			newStatus = "TEMPORARILY_UNAVAILABLE";
			break;
		case LocationProvider.AVAILABLE:
			newStatus = "AVAILABLE";
			break;
		}
	}

}