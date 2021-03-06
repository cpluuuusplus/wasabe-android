package com.ensaitechnomobile.meteo.metier;

import org.json.JSONException;
import org.json.JSONObject;
import com.ensaitechnomobile.exceptions.CityNotFoundException;

/**
 * Cette classe renverra un ou plusieurs objets EtatMeteo � partir d'un JSON
 * 
 * @author nicolas
 * 
 */
public class EtatMeteoJSON {

	/**
	 * Constructeur sans rien
	 */
	public EtatMeteoJSON() {
	}

	/**
	 * 
	 * Initialisateur � partir du JSON de la m�t�o du jour m�me
	 * 
	 * @param json
	 * @throws JSONException
	 *             Si l'objet JSON est invalide
	 * 
	 * @return un objet EtatM�t�o
	 * @throws CityNotFoundException
	 *             Si la ville saisie est incorrecte
	 */
	public EtatMeteo construireEtatMeteoActuelBis(JSONObject json)
			throws JSONException, CityNotFoundException {
		// R�cup�rations JSON
		if (json.getInt("cod") == 404) {
			// La ville n'a pas �t� trouv�e
			throw new CityNotFoundException("La ville n'a pas �t� trouv�e");
		} else {

			// Objet du JSON : dt
			long dt = json.getLong("dt");

			// Objet du JSON : name
			String name = json.getString("name");

			// Objet du JSON : coord
			double lon = json.getJSONObject("coord").getDouble("lon");
			double lat = json.getJSONObject("coord").getDouble("lat");

			// Objet du JSON : sys
			String sysCountry = json.getJSONObject("sys").getString("country");
			long sysSunrise = json.getJSONObject("sys").getLong("sunrise");
			long sysSunset = json.getJSONObject("sys").getLong("sunset");

			// Objet du JSON : main
			int mainTemp = json.getJSONObject("main").getInt("temp");
			int mainHumidity = json.getJSONObject("main").getInt("humidity");
			int mainPressure = json.getJSONObject("main").getInt("pressure");
			int mainTempMin = json.getJSONObject("main").getInt("temp_min");
			int mainTempMax = json.getJSONObject("main").getInt("temp_max");

			// Objet du JSON : wind
			double windSpeed = json.getJSONObject("wind").getDouble("speed");

			// Objet du JSON : clouds
			double cloudsAll = json.getJSONObject("clouds").getDouble("all");

			// Objet du JSON : weather
			int weatherId = json.getJSONArray("weather").getJSONObject(0)
					.getInt("id");
			String weatherMain = json.getJSONArray("weather").getJSONObject(0)
					.getString("main");
			String weatherDesc = json.getJSONArray("weather").getJSONObject(0)
					.getString("description");
			String weatherIcon = json.getJSONArray("weather").getJSONObject(0)
					.getString("icon");

			// Objet du JSON : rain
			// Description de la pluie
			// Si il n'y a pas de pluie, il n'y a pas d'objet "rain"
			// Il faudrait en th�orie faire selon les valeurs de l'objet
			// objWeather
			double rain3h, rain1h;
			try {
				rain3h = json.getJSONObject("rain").getDouble("3h");

			} catch (JSONException e) {
				rain3h = 0.0;
			}
			try {
				rain1h = json.getJSONObject("rain").getDouble("1h");
			} catch (JSONException e) {
				rain1h = 0.0;
			}

			return (new EtatMeteo(dt, name, lon, lat, sysCountry, sysSunrise,
					sysSunset, mainTemp, mainHumidity, mainPressure,
					mainTempMin, mainTempMax, windSpeed, cloudsAll, weatherId,
					weatherMain, weatherDesc, weatherIcon, rain3h, rain1h));
		}
	}
}
