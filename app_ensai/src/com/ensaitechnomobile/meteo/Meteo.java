package com.ensaitechnomobile.meteo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ensai.appli.R;
import com.ensaitechnomobile.common.metier.City;
import com.ensaitechnomobile.exceptions.CityNotFoundException;
import com.ensaitechnomobile.meteo.metier.EtatMeteo;
import com.ensaitechnomobile.meteo.metier.EtatMeteoJSON;

public class Meteo extends ActionBarActivity implements LocationListener {

	protected static final String TAG = "AMS::Meteo";
	protected static final String APIID = "ef5e65bcdadbcc86a991779742664324";
	private LocationManager lm;
	private double latitude = 48.033333;
	private double longitude = -1.750000;
	private LocationManager locationManager;
	private SearchView searchView;
	private MenuItem searchItem;
	private SimpleDateFormat hFormat = new SimpleDateFormat("HH:mm",
			Locale.FRENCH);

	private SharedPreferences preferences;
	private ScrollView meteoBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meteo);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		meteoBack = (ScrollView) findViewById(R.id.activity_meteo_scroll_view);
		meteoBack.setBackgroundResource(preferences.getInt("METEO_COLOR",
				R.drawable.backmotif_blue));

		locateDevice();
		City city = new City(longitude, latitude);
		locateWithCity(city);
	}

	/**
	 * Permet d'affecter les dernieres coordonnees connues par le device
	 * 
	 * @param loc
	 */
	private void updateLoc(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
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
				latitude = 48.033333;
				longitude = -1.750000;
			}
		}
	}

	// Gestion des preferences pour la meteo
	/**
	 * 
	 * @param nem
	 * @param cxt
	 */
	private void addInPrefs(EtatMeteo nem, Context cxt) {
		// On va chercher un objet preferences a editer
		SharedPreferences preferences = cxt.getSharedPreferences("Meteo",
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		// On ajoute les valeurs
		Log.i(TAG, "Dans ADP : " + nem.getName());
		editor.putLong("dt", nem.getDt());
		editor.putString("name", nem.getName());
		editor.putFloat("lon", (float) nem.getLon());
		editor.putFloat("lat", (float) nem.getLat());
		editor.putString("country", nem.getSysCountry());
		editor.putLong("sunrise", nem.getSysSunrise());
		editor.putLong("sunset", nem.getSysSunset());
		editor.putInt("temp", nem.getMainTemp());
		editor.putInt("humidity", nem.getMainHumidity());
		editor.putInt("pressure", nem.getMainPressure());
		editor.putInt("tempMax", nem.getMainTempMax());
		editor.putInt("tempMin", nem.getMainTempMin());
		editor.putFloat("wind", (float) nem.getWindSpeed());
		editor.putFloat("clouds", (float) nem.getCloudsAll());
		editor.putString("main", nem.getWeatherMain());
		editor.putString("desc", nem.getWeatherDesc());
		editor.putString("urlIcon", nem.getWeatherIcon());
		editor.putFloat("rain3", (float) nem.getRain3h());
		editor.putFloat("rain1", (float) nem.getRain1h());

		// On committe les pr�f�rences
		editor.commit();
	}

	/**
	 * Lancez moi sur un thread UI !! Recupere la meteo stockee dans les
	 * preferences
	 * 
	 * 
	 * 
	 */
	private void displayPrefs(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences("Meteo",
				Context.MODE_PRIVATE);

		TextView txt_name = (TextView) findViewById(R.id.activity_meteo_name);
		txt_name.setText(prefs.getString("name", null) + " ("
				+ prefs.getString("country", null) + ")");

		TextView txt_lat = (TextView) findViewById(R.id.activity_meteo_table1_row_latitude_info);
		txt_lat.setText(Math.round(prefs.getFloat("lat", 0) * 100) / 100.0 + "");

		TextView txt_lon = (TextView) findViewById(R.id.activity_meteo_table1_row_longitude_info);
		txt_lon.setText(Math.round(prefs.getFloat("lon", 0) * 100) / 100.0 + "");

		TextView txt_sunrise = (TextView) findViewById(R.id.activity_meteo_table1_row_sunrise_info);
		txt_sunrise.setText(hFormat.format(new Date(
				prefs.getLong("sunrise", 0) * 1000)));

		TextView txt_sunset = (TextView) findViewById(R.id.activity_meteo_table1_row_sunset_info);
		txt_sunset.setText(hFormat.format(new Date(
				prefs.getLong("sunset", 0) * 1000)));

		TextView txt_dt = (TextView) findViewById(R.id.activity_meteo_hour);
		txt_dt.setText(getString(R.string.meteo_hour) + "  "
				+ hFormat.format(new Date(prefs.getLong("dt", 0) * 1000)));

		TextView txt_temp = (TextView) findViewById(R.id.activity_meteo_table2_row_temperature_info);
		txt_temp.setText(prefs.getInt("temp", 99)
				+ getString(R.string.meteo_deg)
				+ getString(R.string.meteo_fluctuate)
				+ Math.round((prefs.getInt("tempMax", 99) - prefs.getInt(
						"tempMin", 99)) * 10 / 2) / 10.0
				+ getString(R.string.meteo_deg));

		TextView txt_humidity = (TextView) findViewById(R.id.activity_meteo_table2_row_humidity_info);
		txt_humidity.setText(prefs.getInt("humidity", 99)
				+ getString(R.string.meteo_percent));

		TextView txt_rain = (TextView) findViewById(R.id.activity_meteo_table2_row_rain_info);
		if (prefs.getFloat("rain3", 0) != 0) {
			txt_rain.setText(Math.round(prefs.getFloat("rain3", 0) * 100)
					/ 100.0 + getString(R.string.meteo_level_water3));
		} else {
			if (prefs.getFloat("rain1", 0) != 0) {
				// Il y a de la pluie a 1h
				txt_rain.setText(Math.round(prefs.getFloat("rain1", 0) * 100)
						/ 100.0 + getString(R.string.meteo_level_water3));
			} else {
				txt_rain.setText(getString(R.string.meteo_level_water0));
			}
		}

		TextView txt_pressure = (TextView) findViewById(R.id.activity_meteo_table2_row_pressure_info);
		txt_pressure.setText(prefs.getInt("pressure", 99)
				+ getString(R.string.meteo_pressure));

		TextView txt_wind = (TextView) findViewById(R.id.activity_meteo_table2_row_wind_info);
		txt_wind.setText(Math.round(prefs.getFloat("wind", 0) * 3.6 * 100)
				/ 100.0 + getString(R.string.meteo_speed));

		TextView txt_clouds = (TextView) findViewById(R.id.activity_meteo_table2_row_clouds_info);
		txt_clouds.setText(Math.round(prefs.getFloat("clouds", 0) * 100)
				/ 100.0 + getString(R.string.meteo_percent));

		TextView txt_main = (TextView) findViewById(R.id.activity_meteo_table2_row_main_info);
		txt_main.setText(prefs.getString("main", null) + " ("
				+ prefs.getString("desc", null) + ")");
		ImageView icon = (ImageView) findViewById(R.id.activity_meteo_img);
		new ImageDownloader(icon).execute(prefs.getString("urlIcon", null));

	}

	/**
	 * Prepare l'URL a partir de coordonnees
	 * 
	 * @param apid
	 * @param loc
	 * @param coordX
	 * @param coordY
	 * @param previsions
	 * @return
	 */
	private String prepareURL(String apid, City loc, int coordX, int coordY,
			boolean previsions) {
		String res = getString(R.string.meteo_URL);
		if (previsions) {
			res += "forecast/Daily";
		} else {
			res += "weather";
		}

		if (loc.hasVille()) {
			res += "?q=" + loc.getVille();
		} else {
			if (loc.hasLongLat()) {
				res += "?lon=" + loc.getLongitude() + "&lat="
						+ loc.getLatitude();
			} else {
				Log.w(TAG, "Crash");
			}
		}

		res += "&units=metric";
		res += "&mode=json";
		res += "&APPID=" + apid;
		Log.v("AMS::Meteo", "URL de recuperation des donnees meteo : " + res);
		return res;
	}

	/**
	 * Methode appelee dans l'Async task pour telecharger les donnees
	 * 
	 * @param urlMeteo
	 */
	private void importMeteoInPrefs(String urlMeteo) {
		// On recupere le JSON a partir de l'URL
		URL url;
		try {
			url = new URL(urlMeteo);
			HttpURLConnection urlConnection;
			urlConnection = (HttpURLConnection) url.openConnection();
			BufferedInputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			String input = readStream(in);
			JSONObject json = new JSONObject(input);
			if (json.getInt("cod") == 404) {
				// La ville n'a pas ete trouvee
				throw new CityNotFoundException(
						getString(R.string.CityNotFoundException_message));
			} else {
				Log.i(TAG, input);

				// On transforme en meteo
				EtatMeteoJSON mjson = new EtatMeteoJSON();
				EtatMeteo em = mjson.construireEtatMeteoActuelBis(json);
				Log.i(TAG, em.toString());
				addInPrefs(em, Meteo.this);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(Meteo.this,
							getString(R.string.url_error_meteo),
							Toast.LENGTH_LONG).show();
				}
			});
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
							Meteo.this,
							getString(R.string.CityNotFoundException_toast,
									city), Toast.LENGTH_LONG).show();
				}
			});
			e.printStackTrace();
		}
	}

	/**
	 * Recupere la meteo dans la localite saisie et met a jour les barres
	 * 
	 * @param loc
	 */
	private void locateWithCity(City loc) {
		String urlMeteo = prepareURL(APIID, loc, 0, 0, false);
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		if (isConnected) {
			new LayoutRefresher().execute(urlMeteo);
		} else {
			Toast.makeText(Meteo.this, R.string.meteo_internet_conection_error,
					Toast.LENGTH_LONG).show();
		}
	}

	// Implementation du LocationListener Portee: 3 methodes suivantes Mot
	// d'ordre : on s'en fiche

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("Latitude", "disable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("Latitude", "enable");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");
	}

	@Override
	protected void onResume() {
		super.onResume();
		lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		if (lm.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,
					0, this);

		if (lm.getAllProviders().contains(LocationManager.GPS_PROVIDER))
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, 0,
					this);
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	// Implementation du menu

	/**
	 * Methode permettant de cocher la bonne couleur de background
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(
				preferences.getInt("METEO_ITEM", R.id.action_bar_meteo_blue))
				.setChecked(true);
		return true;
	}

	/**
	 * Methode permettant de mettre en place l'action bar
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.layout.action_bar_meteo, menu);
		searchItem = menu.findItem(R.id.action_bar_meteo_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		searchView.setQueryHint(getString(R.string.find_city));
		searchView.setOnQueryTextListener(queryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Methode qui se declenchera au clic sur un item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a ete clique grace a son id et on declenche une
		// action
		if (item.getItemId() == R.id.action_bar_meteo_find) {
			locateDevice();
			City city = new City(longitude, latitude);
			locateWithCity(city);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_blue) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_blue);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_blue);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_green) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_green);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_green);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_orange) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_orange);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_orange);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_pink) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_pink);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_pink);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_gold) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_gold);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_gold);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_meteo_darkred) {
			Editor edit = preferences.edit();
			edit.putInt("METEO_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("METEO_ITEM", item.getItemId());
			edit.commit();
			meteoBack.setBackgroundResource(R.drawable.backmotif_darkred);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else
			return false;
	}

	private final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextChange(String newText) {
			// Do something
			return true;
		}

		@SuppressLint("NewApi")
		@Override
		public boolean onQueryTextSubmit(String query) {
			String city = searchView.getQuery() + "";
			locateWithCity(new City(city));
			searchView.clearFocus();
			return false;
		}
	};

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

	/**
	 * Classe permettant d'avoir un icon pour la m�t�o en AsyncTask
	 * 
	 * @author Jeff
	 * 
	 */
	class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		String url;

		public ImageDownloader(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String url = urls[0];
			Bitmap mIcon = null;
			try {
				InputStream in = new java.net.URL(url).openStream();
				mIcon = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
			}
			return mIcon;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

	/**
	 * Classe permettant de mettre � jour les informations d'une nouvelle ville
	 * en asyncTask
	 * 
	 * @author Jeff
	 * 
	 */
	private class LayoutRefresher extends AsyncTask<String, Void, Void> {

		private ProgressDialog progressDialog;

		public LayoutRefresher() {
			this.progressDialog = new ProgressDialog(Meteo.this);
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
			importMeteoInPrefs(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			displayPrefs(Meteo.this);
			if (this.progressDialog.isShowing()) {
				this.progressDialog.dismiss();
			}
		}
	}
}
