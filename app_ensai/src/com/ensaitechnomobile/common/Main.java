package com.ensaitechnomobile.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Toast;

import com.ensai.appli.R;
import com.ensaitechnomobile.agenda.Agenda;
import com.ensaitechnomobile.agenda.metier.LessonItem;
import com.ensaitechnomobile.meteo.Meteo;
import com.ensaitechnomobile.osm.OSM;
import com.ensaitechnomobile.web.view.Ent;
import com.ensaitechnomobile.web.view.Mails;
import com.ensaitechnomobile.web.view.Pamplemousse;

public class Main extends ActionBarActivity {

	public static final String TAG = "Menu principal";
	public ArrayAdapter<LessonItem> adapter;
	private Intent intent = null;
	private SharedPreferences preferences;
	private ScrollView mainBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		mainBack = (ScrollView) findViewById(R.id.activity_main_scroll_view);
		mainBack.setBackgroundResource(preferences.getInt("MAIN_COLOR",
				R.drawable.backmotif_blue));
	}

	// Impl�mentation des listeners bouton
	public void onClickPampViewer(View v) {
		Intent intent = new Intent(getBaseContext(), Agenda.class);
		if (intent != null)
			startActivity(intent);
	}

	public void onClickMail(View v) {
		Intent intent = new Intent(getBaseContext(), Mails.class);
		if (intent != null)
			startActivity(intent);
	}

	public void onClickNotes(View v) {
		Intent intent = new Intent(getBaseContext(), Pamplemousse.class);
		if (intent != null)
			startActivity(intent);
	}
	
	public void onClickEnt(View v) {
		Intent intent = new Intent(getBaseContext(), Ent.class);
		if (intent != null)
			startActivity(intent);
	}

	public void geolocalisation(View v) {
		intent = new Intent(this.getBaseContext(), OSM.class);
		if (intent != null) {
			startActivity(intent);
		}
	}

	public void meteo(View v) {
		intent = new Intent(this.getBaseContext(), Meteo.class);
		if (intent != null) {
			startActivity(intent);
		}
	}

	// Impl�mentation du menu
	/**
	 * M�thode permettant de cocher la bonne couleur de background
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(
				preferences.getInt("MAIN_ITEM", R.id.action_bar_main_blue))
				.setChecked(true);
		return true;
	}

	/**
	 * M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du
	 * t�l�phone
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.action_bar_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	// M�thode qui se d�clenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une
		// action

		if (item.getItemId() == R.id.action_bar_main_user) {
			Intent intent = new Intent(getBaseContext(), Authentification.class);
			if (intent != null)
				startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_credits) {
			Intent intent = new Intent(getBaseContext(), Credits.class);
			if (intent != null)
				startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_blue) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_blue);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_blue);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_blue);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_blue);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_blue);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_blue);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_blue);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_blue);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_blue);
			Toast toast = Toast.makeText(
					Main.this,
					getString(R.string.activity_main_theme_change,
							getString(R.string.color_blue)), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_green) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_green);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_green);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_green);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_green);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_green);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_green);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_green);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_green);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_green);
			Toast toast = Toast
					.makeText(
							Main.this,
							getString(R.string.activity_main_theme_change,
									getString(R.string.color_green)),
							Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_orange) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_orange);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_orange);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_orange);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_orange);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_orange);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_orange);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_orange);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_orange);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_orange);
			Toast toast = Toast.makeText(
					Main.this,
					getString(R.string.activity_main_theme_change,
							getString(R.string.color_orange)),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_pink) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_pink);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_pink);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_pink);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_pink);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_pink);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_pink);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_pink);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_pink);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_pink);
			Toast toast = Toast.makeText(
					Main.this,
					getString(R.string.activity_main_theme_change,
							getString(R.string.color_pink)), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_gold) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_gold);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_gold);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_gold);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_gold);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_gold);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_gold);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_gold);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_gold);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_gold);
			Toast toast = Toast.makeText(
					Main.this,
					getString(R.string.activity_main_theme_change,
							getString(R.string.color_gold)), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else if (item.getItemId() == R.id.action_bar_main_darkred) {
			Editor edit = preferences.edit();
			edit.putInt("MAIN_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("WEB_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("AUTH_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("AGENDA_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("METEO_COLOR", R.drawable.backmotif_darkred);
			edit.putInt("MAIN_ITEM", item.getItemId());
			edit.putInt("AUTH_ITEM", R.id.action_bar_authentification_darkred);
			edit.putInt("AGENDA_ITEM", R.id.action_bar_agenda_darkred);
			edit.putInt("METEO_ITEM", R.id.action_bar_meteo_darkred);
			edit.commit();
			mainBack.setBackgroundResource(R.drawable.backmotif_darkred);
			Toast toast = Toast.makeText(
					Main.this,
					getString(R.string.activity_main_theme_change,
							getString(R.string.color_darkred)),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			return true;
		} else
			return false;
	}
}
