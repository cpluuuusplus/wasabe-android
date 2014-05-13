package com.ensaitechnomobile.geoloc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.ensai.appli.R;
import com.ensaitechnomobile.meteolocale.MeteoJSON;
import com.ensaitechnomobile.metier.CityNotFoundException;
import com.ensaitechnomobile.metier.EtatMeteo;
import com.ensaitechnomobile.metier.Localite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

public class reNewGeolocalisation extends ActionBarActivity {

	private MapView myOpenMapView;
	private MapController myMapController;
	protected static final String APIID = "ef5e65bcdadbcc86a991779742664324";
	protected static final String TAG = "OSM::";
	private double lon, lat;

	LocationManager locationManager;

	ArrayList<OverlayItem> overlayItemArray;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geoloc_osm);

		myOpenMapView = (MapView) findViewById(R.id.mapview);
		myOpenMapView.setTileSource(TileSourceFactory.CYCLEMAP);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = (MapController) myOpenMapView.getController();
		myMapController.setZoom(15);

		// --- Create Overlay
		overlayItemArray = new ArrayList<OverlayItem>();

		DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(
				this);
		MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(
				overlayItemArray, null, defaultResourceProxyImpl);
		myOpenMapView.getOverlays().add(myItemizedIconOverlay);
		// ---

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// for demo, getLastKnownLocation from GPS only, not from NETWORK
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			updateLoc(lastLocation);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, myLocationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		locationManager.removeUpdates(myLocationListener);
	}

	private void updateLoc(Location loc) {
		GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(),
				loc.getLongitude());
		myMapController.setCenter(locGeoPoint);
		setOverlayLoc(loc);
	}

	private void setOverlayLoc(Location overlayloc) {
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		// ---
		overlayItemArray.clear();

		OverlayItem newMyLocationItem = new OverlayItem("My Location",
				"My Location", overlocGeoPoint);
		overlayItemArray.add(newMyLocationItem);
		// ---
	}

	private LocationListener myLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			updateLoc(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

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

	};

	// private SearchView searchView;
	// private MenuItem searchItem;
	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.layout.action_localisation, menu);
	// searchItem = menu.findItem(R.id.action_search);
	// searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	// searchView.setOnQueryTextListener(queryTextListener);
	// return super.onCreateOptionsMenu(menu);
	// }
	//
	// final SearchView.OnQueryTextListener queryTextListener = new
	// SearchView.OnQueryTextListener() {
	// @Override
	// public boolean onQueryTextChange(String newText) {
	// // Do something
	// return true;
	// }
	//
	// @SuppressLint("NewApi")
	// @Override
	// public boolean onQueryTextSubmit(String query) {
	// String city = searchView.getQuery() + "";
	// recupererMeteoActuelleParLocalite(new Localite(city));
	// searchView.clearFocus();
	// return false;
	// }
	// };

	//
	// public void recupererMeteoActuelleParLocalite(Localite loc) {
	// String urlMeteo = urlPreparerMeteo(APIID, loc, 0, 0, false);
	// syncMeteo(urlMeteo, this.getBaseContext());
	// }
	//
	// String urlPreparerMeteo(String apid, Localite loc, int coordX, int
	// coordY,
	// boolean previsions) {
	// String res = "http://api.openweathermap.org/data/2.5/";
	// if (previsions) {
	// res += "forecast/Daily";
	// } else {
	// res += "weather";
	// }
	//
	// if (loc.hasVille()) {
	// res += "?q=" + loc.getVille();
	// } else {
	// if (loc.hasLongLat()) {
	// res += "?lon=" + loc.getLongitude() + "&lat="
	// + loc.getLatitude();
	// }
	// }
	//
	// res += "&units=metric";
	// res += "&mode=json";
	// res += "&APPID=" + apid;
	// Log.v("AMS::Meteo", "URL de r�cup�ration des donn�es m�t�o : " + res);
	// return res;
	// }
	//
	// private void syncMeteo(final String urlString, final Context ctx) {
	// // Get all fields to be updated
	//
	// Runnable code = new Runnable() {
	// URL url = null;
	//
	// public void run() {
	// try {
	// // On r�cup�re le JSON a partir de l'URL
	// url = new URL(urlString);
	// HttpURLConnection urlConnection;
	// urlConnection = (HttpURLConnection) url.openConnection();
	// BufferedInputStream in = new BufferedInputStream(
	// urlConnection.getInputStream());
	// String input = readStream(in);
	// JSONObject json = new JSONObject(input);
	// Log.i(TAG, input);
	// // On transforme en m�t�o
	// MeteoJSON mjson = new MeteoJSON();
	// EtatMeteo em = mjson.construireEtatMeteoActuel(json);
	// lon = json.getJSONObject("coord").getDouble("lon");
	// lat = json.getJSONObject("coord").getDouble("lon");
	// Log.i(TAG, json.toString());
	// Log.i(TAG, em.toString());
	//
	// runOnUiThread(new Runnable() {
	// public void run() {
	// addLocation(lat, lon);
	// }
	// });
	//
	// // TODO me d�brouiller pour maj
	//
	// } catch (MalformedURLException e) {
	// Log.e(TAG, "URL malform�e");
	// e.printStackTrace();
	// } catch (IOException e) {
	// Log.e(TAG, "Exception d'E/S");
	// e.printStackTrace();
	// } catch (JSONException e) {
	// Log.e(TAG, "Exception JSON");
	// e.printStackTrace();
	// } catch (CityNotFoundException e) {
	// runOnUiThread(new Runnable() {
	// public void run() {
	// Toast.makeText(
	// ctx,
	// "La ville est invalide, veuillez saisir une ville valide",
	// Toast.LENGTH_LONG).show();
	// }
	// });
	// }
	// }
	// };
	// new Thread(code).start();
	// }
	//
	// private void addLocation(double lat, double lng) {
	// // ---Add a location marker---
	//
	// GeoPoint p = new GeoPoint(lat* 1E3, lng);
	//
	// myMapController.animateTo(p);
	// myMapController.setCenter(p);
	// overlayItemArray = new ArrayList<OverlayItem>();
	// // Put overlay icon a little way from map center
	// overlayItemArray
	// .add(new OverlayItem("Here u r", "SampleDescription", p));
	// }
	//
	// /**
	// * Lit un flux de donn�es et retourne le string correspondant
	// *
	// * @param inputStream
	// * @return
	// * @throws IOException
	// */
	// public String readStream(InputStream inputStream) throws IOException {
	// BufferedReader reader = new BufferedReader(new InputStreamReader(
	// inputStream));
	// String ligne = null;
	// String contenu = "";
	// while ((ligne = reader.readLine()) != null) {
	// contenu += ligne;
	// }
	// return contenu;
	// }

	public class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem> {

		public MyItemizedIconOverlay(
				List<OverlayItem> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy) {
			super(pList, pOnItemGestureListener, pResourceProxy);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void draw(Canvas canvas, MapView mapview, boolean arg2)
				throws IndexOutOfBoundsException {
			// TODO Auto-generated method stub
			super.draw(canvas, mapview, arg2);

			if (overlayItemArray != null && overlayItemArray.size() != 0) {

				GeoPoint in = overlayItemArray.get(0).getPoint();

				Point out = new Point();
				mapview.getProjection().toPixels(in, out);

				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_action_location_found);
				canvas.drawBitmap(bm, out.x - bm.getWidth() / 2, // shift the
																	// bitmap
																	// center
						out.y - bm.getHeight() / 2, // shift the bitmap center
						null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) {

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
			return true;

		}
	}
}
