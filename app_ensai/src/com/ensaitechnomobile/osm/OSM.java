package com.ensaitechnomobile.osm;

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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.http.IHttpClientFactory;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ensai.appli.R;
import com.ensaitechnomobile.common.metier.City;
import com.ensaitechnomobile.exceptions.CityNotFoundException;

public class OSM extends ActionBarActivity {

	private SearchView searchView;
	private MenuItem searchItem;
	private MapView myOpenMapView;
	private MapController myMapController;
	protected static final String APIID = "ef5e65bcdadbcc86a991779742664324";
	protected static final String TAG = "OSM::";
	private double longitude, latitude;
	private LocationManager locationManager;
	private ArrayList<OverlayItem> overlayItemArray;
	private String country = null;
	private SharedPreferences preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_osm);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		HttpClientFactory.setFactoryInstance(new IHttpClientFactory() {
			public HttpClient createHttpClient() {
				final DefaultHttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
						"ensaitechnomobiles.AMS");
				return client;
			}
		});

		myOpenMapView = (MapView) findViewById(R.id.mapview);

		int item = preferences.getInt("MAP", R.id.action_bar_osm_mapnik);
		if (item == R.id.action_bar_osm_cyclemap) {

			myOpenMapView.setTileSource(TileSourceFactory.CYCLEMAP);

		} else if (item == R.id.action_bar_osm_mapnik) {

			myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);

		} else if (item == R.id.action_bar_osm_mapquestosm) {

			myOpenMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);

		} else if (item == R.id.action_bar_osm_public_transport) {

			myOpenMapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);

		} else if (item == R.id.action_bar_osm_mapquestaerial) {
			;
			myOpenMapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);

		} else
			myOpenMapView.setTileSource(TileSourceFactory.CYCLEMAP);

		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = (MapController) myOpenMapView.getController();
		myMapController.setZoom(9);

		// --- Create Overlay
		overlayItemArray = new ArrayList<OverlayItem>();

		DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(
				this);
		MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(
				overlayItemArray, null, defaultResourceProxyImpl);
		myOpenMapView.getOverlays().add(myItemizedIconOverlay);
		// ---
		locateDevice();
	}

	/**
	 * Localiser le device
	 */
	private void locateDevice() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			updateLoc(lastLocation);
		} else {
			lastLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastLocation != null) {
				updateLoc(lastLocation);
			} else {
				Location loc = new Location("");
				loc.setLatitude(48.13);
				loc.setLongitude(-1.67);
				updateLoc(loc);
			}
		}
	}

	// Impl�mentation du menu
	/**
	 * M�thode permettant de cocher la bonne couleur de background
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(preferences.getInt("MAP", R.id.action_bar_osm_cyclemap))
				.setChecked(true);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.layout.action_bar_osm, menu);
		searchItem = menu.findItem(R.id.action_bar_osm_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		searchView.setQueryHint(getString(R.string.find_city));
		searchView.setOnQueryTextListener(queryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * M�thode qui se d�clenchera au clic sur un item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une
		// action
		Editor editor = preferences.edit();
		if (item.getItemId() == R.id.action_bar_osm_find) {
			locateDevice();
			return true;
		} else if (item.getItemId() == R.id.action_bar_osm_cyclemap) {
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			editor.putInt("MAP", item.getItemId());
			editor.commit();
			myOpenMapView.setTileSource(TileSourceFactory.CYCLEMAP);
			return true;
		} else if (item.getItemId() == R.id.action_bar_osm_mapnik) {
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			editor.putInt("MAP", item.getItemId());
			editor.commit();
			myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);
			return true;
		} else if (item.getItemId() == R.id.action_bar_osm_mapquestosm) {
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			editor.putInt("MAP", item.getItemId());
			editor.commit();
			myOpenMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
			return true;
		} else if (item.getItemId() == R.id.action_bar_osm_public_transport) {
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			editor.putInt("MAP", item.getItemId());
			editor.commit();
			myOpenMapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
			return true;
		} else if (item.getItemId() == R.id.action_bar_osm_mapquestaerial) {
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			editor.putInt("MAP", item.getItemId());
			editor.commit();
			myOpenMapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			return true;
		} else
			return false;
	}

	// Impl�mentation de la search View

	/**
	 * Argument permettant de personnaliser le listener de la searchView
	 */
	private final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

		@Override
		public boolean onQueryTextSubmit(String query) {
			String city = searchView.getQuery() + "";
			findNewCity(new City(city));
			searchView.clearFocus();
			return false;
		}

		@Override
		public boolean onQueryTextChange(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	/**
	 * Pr�pare l'URL pour la connection en ligne
	 * 
	 * @param apid
	 * @param loc
	 * @param coordX
	 * @param coordY
	 * @return
	 */
	private String prepareURL(String apid, City loc, int coordX, int coordY) {
		String res = getString(R.string.osm_URL);
		if (loc.hasVille()) {
			res += "?q=" + loc.getVille();
		} else {
			if (loc.hasLongLat()) {
				res += "?lon=" + loc.getLongitude() + "&lat="
						+ loc.getLatitude();
			}
		}
		res += "&units=metric";
		res += "&mode=json";
		res += "&APPID=" + apid;
		Log.v("AMS::Meteo", "URL de recuperation des donnees meteo : " + res);
		return res;
	}

	/**
	 * Recherche d'une nouvelle ville
	 * 
	 * @param loc
	 */
	private void findNewCity(City loc) {
		String cityURL = prepareURL(APIID, loc, 0, 0);
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		if (isConnected) {
			new LayoutRefresher().execute(cityURL);
		} else {
			Toast.makeText(OSM.this, R.string.osm_internet_conection_error,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * On recupere les coordonnees de la ville recherchee
	 * 
	 * @param cityURL
	 */
	private void downloadCoord(String cityURL) {
		// On r�cup�re le JSON a partir de l'URL
		URL url;
		try {
			// On r�cup�re le JSON a partir de l'URL
			url = new URL(cityURL);
			HttpURLConnection urlConnection;
			urlConnection = (HttpURLConnection) url.openConnection();
			BufferedInputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			String input = readStream(in);
			JSONObject json = new JSONObject(input);
			if (json.getInt("cod") == 404) {
				// La ville n'a pas �t� trouv�e
				throw new CityNotFoundException(
						getString(R.string.CityNotFoundException_message));
			} else {
				Log.i(TAG, json.toString());
				longitude = json.getJSONObject("coord").getDouble("lon");
				latitude = json.getJSONObject("coord").getDouble("lat");
				country = json.getJSONObject("sys").getString("country");
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CityNotFoundException e) {
			// TODO Auto-generated catch block
			runOnUiThread(new Runnable() {
				public void run() {
					String city = searchView.getQuery() + "";
					Toast.makeText(
							OSM.this,
							getString(R.string.CityNotFoundException_toast,
									city), Toast.LENGTH_LONG).show();
				}
			});
			e.printStackTrace();
		}
	}

	/**
	 * Lit un flux de donn�es et retourne le string correspondant
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private String readStream(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String ligne = null;
		String contenu = "";
		while ((ligne = reader.readLine()) != null) {
			contenu += ligne;
		}
		return contenu;
	}

	// Gestion de la map

	/**
	 * Positionne la carte � l'�cran
	 * 
	 * @param loc
	 */
	private void updateLoc(Location loc) {
		GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(),
				loc.getLongitude());
		myMapController.setCenter(locGeoPoint);
		setOverlayLoc(loc);
	}

	/**
	 * Met en forme la carte avec l'icone
	 * 
	 * @param overlayloc
	 */
	private void setOverlayLoc(Location overlayloc) {
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		// ---
		overlayItemArray.clear();
		OverlayItem newMyLocationItem = new OverlayItem("My Location",
				"My Location", overlocGeoPoint);
		overlayItemArray.add(newMyLocationItem);
		// ---
	}

	/**
	 * M�thode permettant l'animation d'une ville � une autre
	 * 
	 * @param lat
	 * @param lng
	 */
	private void moveToSearchedCity(double lat, double lng) {
		// ---Add a location marker---
		GeoPoint p = new GeoPoint(lat, lng);
		myMapController.animateTo(p);
		myMapController.setCenter(p);
		overlayItemArray = new ArrayList<OverlayItem>();

		// Put overlay icon a little way from map center
		overlayItemArray
				.add(new OverlayItem("Here u r", "SampleDescription", p));
	}

	/**
	 * Classe permettant de personnaliser la map
	 */
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
						R.drawable.ic_position);
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

		public boolean onDoubleTap(MotionEvent e, MapView mapView) {

			GeoPoint p = (GeoPoint) mapView.getProjection().fromPixels(
					(int) e.getX(), (int) e.getY());
			myMapController.animateTo(p);
			// myMapController.setCenter(p);
			overlayItemArray = new ArrayList<OverlayItem>();

			// Put overlay icon a little way from map center
			overlayItemArray.add(new OverlayItem("Here u r",
					"SampleDescription", p));
			mapView.getController().zoomIn();
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
			return true;
		}

	}

	/**
	 * Classe permettant de changer de ville en AsyncTask
	 * 
	 * @author Jeff
	 * 
	 */
	private class LayoutRefresher extends AsyncTask<String, Void, Void> {

		private ProgressDialog progressDialog;

		public LayoutRefresher() {
			this.progressDialog = new ProgressDialog(OSM.this);
			this.progressDialog.setTitle(getString(R.string.searching_city));
			this.progressDialog.setMessage(getString(R.string.move_to_city));
		}

		@Override
		protected void onPreExecute() {
			this.progressDialog.show();
			this.progressDialog.setCanceledOnTouchOutside(false);
		}

		@Override
		protected Void doInBackground(String... params) {
			downloadCoord(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (country != null) {
				String city = searchView.getQuery() + "";
				city += " (" + country + ")";
				searchView.setQuery(city, false);
			}
			moveToSearchedCity(latitude, longitude);
			if (this.progressDialog.isShowing()) {
				this.progressDialog.dismiss();
			}
		}
	}
}
