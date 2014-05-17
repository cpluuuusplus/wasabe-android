package com.ensaitechnomobile.meteo.locale;

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

import com.ensai.appli.R;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.ensaitechnomobile.metier.CityNotFoundException;
import com.ensaitechnomobile.metier.EtatMeteo;
import com.ensaitechnomobile.metier.Localite;

public class MeteoPrincipal extends ActionBarActivity implements
		LocationListener {

	protected static final String TAG = "AMS::Meteo";
	protected static final String APIID = "ef5e65bcdadbcc86a991779742664324";
	private LocationManager lm;
	private double latitude = 48.033333;
	private double longitude = -1.750000;
	private SearchView searchView;
	private MenuItem searchItem;
	private SimpleDateFormat hFormat = new SimpleDateFormat("HH:mm",
			Locale.FRENCH);
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meteo_principale);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		progressDialog = new ProgressDialog(this);

		// TODO localisation

		Localite localite = new Localite(longitude, latitude);
		Log.i(TAG, "Localisation lue avec succ�s : " + localite.toString());
		recupererMeteoActuelleParLocalite(localite);

		// String urlMeteo = urlPreparerMeteo(APIID, new Localite("Eindhoven"),
		// 0,
		// 0, false);
		// syncMeteo(urlMeteo, this.getBaseContext());

	}

	/**
	 * Permet d'ajouter un �l�ment m�teo dans les pr�fe�rences
	 * 
	 * @param cxt
	 *            un contexte (Activity.getBaseContext)
	 * @param em
	 *            un �tat m�t�o
	 * 
	 */
	void ajouterDansPreferences(EtatMeteo em, Context cxt) {
		// On va chercher un objet pr�f�rences � �diter
		SharedPreferences preferences = cxt.getSharedPreferences("MeteoLocal",
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		// On ajoute les valeurs
		Log.i(TAG, "Dans ADP : " + em.getLoc().toString());
		editor.putString("localite", em.getLoc().getVille());
		editor.putString("typeMeteo", em.getTypeMet().toString());
		editor.putInt("tempMax", (int) em.getTempMax());
		editor.putInt("tempMin", (int) em.getTempMin());
		if (em.getRain3() != 0.0) {
			editor.putInt("rain3", (int) (1000 * em.getRain3()));
		} else {
			editor.putInt("rain3", 0);
		}
		if (em.getRain1() != 0.0) {
			editor.putInt("rain1", (int) (1000 * em.getRain1()));
		} else {
			editor.putInt("rain1", 0);

		}
		editor.putInt("wind", (int) em.getWindSpeed());
		editor.putInt("clouds", (int) em.getClouds());
		editor.putString("country", em.getCountry());
		editor.putLong("sunrise", (Long) em.getSunrise());
		editor.putLong("sunset", (Long) em.getSunset());
		editor.putLong("pressure", (Long) em.getPressure());
		// On committe les pr�f�rences
		editor.commit();
	}

	/**
	 * Lancez moi sur un thread UI !! R�cup�re la m�t�o stock�e dans les
	 * pr�f�rences
	 * 
	 * 
	 * 
	 */
	void actualiserMeteoPreferences(Context cxt) {
		SharedPreferences prefs = cxt.getSharedPreferences("MeteoLocal",
				Context.MODE_PRIVATE);
		// On va chercher les textbox
		TextView txt_loc, txt_temperature, txt_pluie, txt_vent, txt_nuages, txt_sunrise, txt_pressure, txt_sunset;
		txt_loc = (TextView) findViewById(R.id.afficher_localite_meteo);
		txt_temperature = (TextView) findViewById(R.id.info_temp);
		txt_pluie = (TextView) findViewById(R.id.info_pluie);
		txt_vent = (TextView) findViewById(R.id.info_vent);
		txt_nuages = (TextView) findViewById(R.id.info_nuages);
		txt_sunrise = (TextView) findViewById(R.id.info_sunrise);
		txt_sunset = (TextView) findViewById(R.id.info_sunset);
		txt_pressure = (TextView) findViewById(R.id.info_pressure);
		// On les renseigne

		if (prefs.getString("localite", "Prefs Pas de loc").equals("Bruz")
				&& prefs.getString("typeMeteo", "?").contains("il pleut")) {
			// Si on est a bruz et qu'il pleut ou qu'il pleut un peu,
			// je rajoute "pour changer"
			txt_loc.setText("�  "
					+ prefs.getString("localite", "Prefs Pas de loc") + " ("
					+ prefs.getString("country", null) + "), "
					+ prefs.getString("typeMeteo", "?") + " "
					+ " (pour changer)");

		} else {
			txt_loc.setText("�  "
					+ prefs.getString("localite", "Prefs Pas de loc") + " ("
					+ prefs.getString("country", null) + "), "
					+ prefs.getString("typeMeteo", "?") + " ");
		}
		txt_temperature.setText("Entre " + prefs.getInt("tempMin", -100)
				+ "� et " + prefs.getInt("tempMax", -100) + "�C");
		if (prefs.getInt("rain3", 0) != 0) {
			// Il y a de la pluie � 3h
			txt_pluie.setText(prefs.getInt("rain3", -100) / 1000
					+ "mm de pluie dans les 3h");

		} else {
			if (prefs.getInt("rain1", 0) != 0) {
				// Il y a de la pluie a 1h
				txt_pluie.setText(prefs.getInt("rain1", -100) / 1000
						+ "mm de pluie dans l'heure");
			} else {
				txt_pluie.setText(0 + " mm");
			}
		}
		txt_vent.setText(prefs.getInt("wind", -100) * 3.6 + " km/h");
		txt_nuages.setText(prefs.getInt("clouds", -100) + "%");
		txt_sunrise
				.setText(hFormat.format(new Date(prefs.getLong("sunrise", 0))));
		txt_sunset
				.setText(hFormat.format(new Date(prefs.getLong("sunset", 0))));
		txt_pressure.setText(prefs.getLong("pressure", 0) + " hPa");
	}

	/**
	 * Lit un flux de donn�es et retourne le string correspondant
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public String readStream(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String ligne = null;
		String contenu = "";
		while ((ligne = reader.readLine()) != null) {
			contenu += ligne;
		}
		return contenu;
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

	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	/**
	 * Associ� � un bouton Cette m�thode r�cup�re la localit�
	 * 
	 */
	public void recupererLocalisationAppareil(View v) {
		Localite localite = new Localite(longitude, latitude);
		Log.i(TAG, "Localisation lue avec succ�s : " + localite.toString());
		recupererMeteoActuelleParLocalite(localite);
	}

	/**
	 * Recup�re la m�t�o dans la localit� saisie et met � jour les barres
	 * 
	 * 
	 */
	public void recupererMeteoActuelleParLocalite(Localite loc) {
		String urlMeteo = urlPreparerMeteo(APIID, loc, 0, 0, false);
		syncMeteo(urlMeteo, this.getBaseContext());
	}

	/**
	 * Thread parall�le qui ajoute des donn�es quelquepart .. � partir des infos
	 * sur le web
	 * 
	 * Input : url : l'URL a appeler qui devrait Exemple d'URL :
	 * http://api.openweathermap.org/data/2.5/weather?q=Bruz,fr&units=metric
	 * TODO faire une fonction interm�diaire pour pouvoir saisir la ville
	 */
	private void syncMeteo(final String urlString, final Context ctx) {
		// Get all fields to be updated

		progressDialog.setTitle(getString(R.string.searching_city));
		progressDialog.setMessage(getString(R.string.move_to_city));
		progressDialog.show();

		Runnable code = new Runnable() {
			URL url = null;

			public void run() {
				try {
					// On r�cup�re le JSON a partir de l'URL
					url = new URL(urlString);
					HttpURLConnection urlConnection;
					urlConnection = (HttpURLConnection) url.openConnection();
					BufferedInputStream in = new BufferedInputStream(
							urlConnection.getInputStream());
					String input = readStream(in);
					JSONObject json = new JSONObject(input);
					Log.i(TAG, input);
					// On transforme en m�t�o
					MeteoJSON mjson = new MeteoJSON();
					EtatMeteo em = mjson.construireEtatMeteoActuel(json);
					Log.i(TAG, em.toString());

					ajouterDansPreferences(em, ctx);

					runOnUiThread(new Runnable() {
						public void run() {
							actualiserMeteoPreferences(ctx);
						}
					});

					// TODO me d�brouiller pour maj

				} catch (MalformedURLException e) {
					Log.e(TAG, "URL malform�e");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG, "Exception d'E/S");
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(MeteoPrincipal.this,
									getString(R.string.url_error_meteo),
									Toast.LENGTH_LONG).show();
						}
					});
					e.printStackTrace();
				} catch (JSONException e) {
					Log.e(TAG, "Exception JSON");
					e.printStackTrace();
				} catch (CityNotFoundException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(
									ctx,
									"La ville est invalide, veuillez saisir une ville valide",
									Toast.LENGTH_LONG).show();
						}
					});
				}
				progressDialog.dismiss();
			}
		};
		new Thread(code).start();
	}

	/**
	 * 
	 * 
	 * 
	 * @param apid
	 *            : l'APPID de OpenWeatherMap
	 * @param loc
	 *            : la ville pour laquelle on veut la m�t�o
	 * @param coordX
	 *            : la latitude pour laquelle on veut la m�t�o
	 * @param coordY
	 *            : la longitude pour laquelle on veut la m�t�o
	 * @param previsions
	 *            : si l'on veut des pr�visions (ou implicitement la m�t�o du
	 *            jour m�me
	 * 
	 * @return
	 */

	String urlPreparerMeteo(String apid, Localite loc, int coordX, int coordY,
			boolean previsions) {
		String res = "http://api.openweathermap.org/data/2.5/";
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
				Log.w(TAG, "Ca va planter car localit� incorrecte");
			}
		}

		res += "&units=metric";
		res += "&mode=json";
		res += "&APPID=" + apid;
		Log.v("AMS::Meteo", "URL de r�cup�ration des donn�es m�t�o : " + res);
		return res;
	}

	/**
	 * 
	 * 
	 * Impl�mentation du LocationListener Port�e: 3 m�thodes suivantes Mot
	 * d'ordre : on s'en fiche
	 * 
	 */
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.layout.action_localisation, menu);
		searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setQueryHint(getString(R.string.find_city));
		searchView.setOnQueryTextListener(queryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextChange(String newText) {
			// Do something
			return true;
		}

		@SuppressLint("NewApi")
		@Override
		public boolean onQueryTextSubmit(String query) {
			String city = searchView.getQuery() + "";
			recupererMeteoActuelleParLocalite(new Localite(city));
			searchView.clearFocus();
			// searchItem.collapseActionView();
			return false;
		}
	};

}