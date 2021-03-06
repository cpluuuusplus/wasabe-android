package com.ensaitechnomobile.agenda;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ensai.appli.R;
import com.ensaitechnomobile.agenda.dao.CoursDAO;
import com.ensaitechnomobile.agenda.metier.DayItem;
import com.ensaitechnomobile.agenda.metier.Item;
import com.ensaitechnomobile.agenda.metier.LessonItem;
import com.ensaitechnomobile.agenda.sql.MyOpenHelper;
import com.ensaitechnomobile.common.Authentification;

public class Agenda extends ActionBarActivity {

	public static final String TAG = "MultiService";
	private String baseUrl = "";
	private String id, pass;
	private CoursDAO cdao = new CoursDAO();
	private SimpleDateFormat dFormat = new SimpleDateFormat(
			"EEEE, dd MMMM yyyy\n", Locale.FRENCH);

	public ArrayAdapter<LessonItem> adapter;

	// Creation de la ArrayList qui nous permettra de remplire la listView
	private ListView listeView;
	private JSONArray table = null;
	private SharedPreferences preferences;
	private LinearLayout agendaBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agenda);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		agendaBack = (LinearLayout) findViewById(R.id.activity_agenda_linear_layout);
		agendaBack.setBackgroundResource(preferences.getInt("AGENDA_COLOR",
				R.drawable.backmotif_blue));
		displayLessons();
	}

	/**
	 * Methode permettant d'afficher l'emploi du temps e partir de SQLOpenHelper
	 */
	private void displayLessons() {
		SQLiteOpenHelper helper = new MyOpenHelper(this);
		SQLiteDatabase db = helper.getWritableDatabase();
		ArrayList<LessonItem> ls = cdao.getAll(db);
		if (ls.size() > 0) {
			ArrayList<Item> items = new ArrayList<Item>();
			String day = dFormat.format(new Date(ls.get(0).getDebut()));
			items.add(new DayItem(ls.get(0).getDebut()));
			for (Iterator<LessonItem> iterator = ls.iterator(); iterator
					.hasNext();) {
				LessonItem lessonItem = (LessonItem) iterator.next();
				if (day.equals(dFormat.format(new Date(lessonItem.getDebut())))) {
					items.add(lessonItem);
				} else {
					day = dFormat.format(new Date(lessonItem.getDebut()));
					items.add(new DayItem(lessonItem.getDebut()));
					items.add(lessonItem);
				}
			}
			LessonAdapter adapter = new LessonAdapter(this, items);
			listeView = (ListView) findViewById(R.id.activity_agenda_planning);
			listeView.setAdapter(adapter);
		} else {
			Toast.makeText(Agenda.this, getString(R.string.agenda_null),
					Toast.LENGTH_LONG).show();
		}
		db.close();
	}

	/**
	 * Methode permettant de recuperer l'emploi du temps en ligne
	 */
	private void downloadLessons() {
		// On recupere le JSON a partir de l'URL
		URL url;
		try {
			url = new URL(baseUrl);
			HttpURLConnection urlConnection;
			urlConnection = (HttpURLConnection) url.openConnection();
			BufferedInputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			String input = readStream(in);
			JSONObject json = new JSONObject(input);
			table = json.getJSONArray("events");
			if (table != null && table.length() > 0) {
				SQLiteOpenHelper helper = new MyOpenHelper(Agenda.this);
				SQLiteDatabase db = helper.getWritableDatabase();
				cdao.removeAll(db);
				ArrayList<LessonItem> lsCours = coursToArray(table);
				cdao.insertAll(db, lsCours);
				db.close();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(Agenda.this,
							getString(R.string.url_error_agenda),
							Toast.LENGTH_LONG).show();
				}
			});
			e.printStackTrace();
		}
	}

	/**
	 * Methode permettant de mettre a jour l'URL
	 */
	private void majURL() {
		baseUrl = getString(R.string.agenda_baseURL);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		id = preferences.getString("login", "");
		pass = preferences.getString("password", "");
		if (id.length() == 6 && pass.length() > 0) {
			baseUrl += "?login=" + id;
			baseUrl += "&mdp=" + pass;
		}
	}

	/**
	 * M�thode permettant de recuperer un tableau de cours a partir d'un
	 * JSONArray
	 * 
	 * @param table
	 * @throws JSONException
	 */
	private ArrayList<LessonItem> coursToArray(JSONArray table)
			throws JSONException {
		ArrayList<LessonItem> listeCours = new ArrayList<LessonItem>();
		for (int i = 0; i < table.length(); i++) {
			JSONObject cours = table.getJSONObject(i);
			String nom = cours.getString("nom");
			String salle = cours.getString("salle");
			String uid = cours.getString("uid");
			long debut = cours.getLong("debut");
			long fin = cours.getLong("fin");
			listeCours.add(new LessonItem(debut, fin, nom, salle, uid));
		}
		return listeCours;
	}

	/**
	 * Lire un flux de donnees
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

	// Impl�mentation du menu

	/**
	 * M�thode permettant de cocher la bonne couleur de background
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(
				preferences.getInt("AGENDA_ITEM", R.id.action_bar_agenda_blue))
				.setChecked(true);
		return true;
	}

	/**
	 * Methode qui se declenchera lorsque vous appuierez sur le bouton menu du
	 * telephone
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.action_bar_agenda, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Methode qui se declenchera au clic sur un item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a ete clique grace a son id et on declenche une
		// action
		// agendaback.setBackgroundResource(R.drawable.backmotif_blue);
		if (item.getItemId() == R.id.action_bar_agenda_sync) {
			ConnectivityManager cm = (ConnectivityManager) this
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null
					&& activeNetwork.isConnectedOrConnecting();
			if (isConnected) {
				new LayoutRefresher().execute();
				return true;
			} else {
				Toast.makeText(Agenda.this,
						R.string.agenda_internet_conection_error,
						Toast.LENGTH_LONG).show();
				return false;
			}
		} else if (item.getItemId() == R.id.action_bar_agenda_user) {
			Intent intent = new Intent(this, Authentification.class);
			if (intent != null)
				startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_blue) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_blue);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_blue);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_green) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_green);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_green);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_orange) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_orange);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_orange);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_pink) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_pink);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_pink);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_gold) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_gold);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_gold);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_agenda_darkred) {
			Editor edit = preferences.edit();
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("AGENDA_ITEM", item.getItemId());
			edit.commit();
			agendaBack.setBackgroundResource(R.drawable.backmotif_darkred);
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else
			return false;
	}

	/**
	 * Classe permettant de charger l'emploi du temps en AsyncTask
	 * 
	 * @author Jeff
	 * 
	 */
	private class LayoutRefresher extends AsyncTask<String, Void, Void> {

		private ProgressDialog progressDialog;

		public LayoutRefresher() {
			this.progressDialog = new ProgressDialog(Agenda.this);
			this.progressDialog.setTitle(getString(R.string.agenda_load));
			this.progressDialog
					.setMessage(getString(R.string.agenda_load_desc));
			majURL();
		}

		@Override
		protected void onPreExecute() {

			if (progressDialog == null) {
				progressDialog.setIndeterminate(false);
				progressDialog.setCancelable(false);
			}
			progressDialog.show();

		}

		@Override
		protected Void doInBackground(String... unused) {
			downloadLessons();
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (table != null) {
				Toast.makeText(Agenda.this,
						getString(R.string.agenda_nb_cours, table.length()),
						Toast.LENGTH_LONG).show();
				displayLessons();
			}
			if (Agenda.this.isDestroyed()) { // or call isFinishing() if min sdk
												// version < 17
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				return;
			}
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}
	}

}
