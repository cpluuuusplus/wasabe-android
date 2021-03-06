package com.ensaitechnomobile.meteo.metier;

public class EtatMeteo {
	// attributs

	// Objet du JSON : dt
	long dt;

	// Objet du JSON : name
	String name;

	// Objet du JSON : coord
	double lon, lat;

	// Objet du JSON : sys
	String sysCountry;
	long sysSunrise, sysSunset;

	// Objet du JSON : main
	int mainTemp, mainHumidity, mainPressure, mainTempMin, mainTempMax;

	// Objet du JSON : wind
	double windSpeed;

	// Objet du JSON : clouds
	double cloudsAll;

	// Objet du JSON : weather
	int weatherId;
	String weatherMain, weatherDesc,
			weatherIcon = "http://openweathermap.org/img/w/";

	// Objet du JSON : rain
	double rain3h, rain1h;

	// #################################################
	// Constructeur
	public EtatMeteo(long dt, String name, double lon, double lat,
			String sysCountry, long sysSunrise, long sysSunset, int mainTemp,
			int mainHumidity, int mainPressure, int mainTempMin,
			int mainTempMax, double windSpeed, double cloudsAll, int weatherId,
			String weatherMain, String weatherDesc, String weatherIcon,
			double rain3h, double rain1h) {
		super();
		this.dt = dt;
		this.name = name;
		this.lon = lon;
		this.lat = lat;
		this.sysCountry = sysCountry;
		this.sysSunrise = sysSunrise;
		this.sysSunset = sysSunset;
		this.mainTemp = mainTemp;
		this.mainHumidity = mainHumidity;
		this.mainPressure = mainPressure;
		this.mainTempMin = mainTempMin;
		this.mainTempMax = mainTempMax;
		this.windSpeed = windSpeed;
		this.cloudsAll = cloudsAll;
		this.weatherId = weatherId;
		this.weatherMain = weatherMain;
		this.weatherDesc = weatherDesc;
		this.weatherIcon = this.weatherIcon + weatherIcon + ".png";
		this.rain3h = rain3h;
		this.rain1h = rain1h;
	}

	// #################################################
	// Accesseurs
	public long getDt() {
		return dt;
	}

	public String getName() {
		return name;
	}

	public double getLon() {
		return lon;
	}

	public double getLat() {
		return lat;
	}

	public String getSysCountry() {
		return sysCountry;
	}

	public long getSysSunrise() {
		return sysSunrise;
	}

	public long getSysSunset() {
		return sysSunset;
	}

	public int getMainTemp() {
		return mainTemp;
	}

	public int getMainHumidity() {
		return mainHumidity;
	}

	public int getMainPressure() {
		return mainPressure;
	}

	public int getMainTempMin() {
		return mainTempMin;
	}

	public int getMainTempMax() {
		return mainTempMax;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public double getCloudsAll() {
		return cloudsAll;
	}

	public int getWeatherId() {
		return weatherId;
	}

	public String getWeatherMain() {
		return weatherMain;
	}

	public String getWeatherDesc() {
		return weatherDesc;
	}

	public String getWeatherIcon() {
		return weatherIcon;
	}

	public double getRain3h() {
		return rain3h;
	}

	public double getRain1h() {
		return rain1h;
	}

	@Override
	public String toString() {
		return (this.dt + "<-dt, " + this.name + "<- name " + this.lon
				+ "<- lon " + this.lat + "<- lat " + this.sysCountry
				+ "<- sysCountry " + this.sysSunrise + "<- sysSunrise "
				+ this.sysSunset + "<- sysSunset " + this.mainTemp
				+ "<- mainTemp " + this.mainHumidity + "<- mainHumidity "
				+ this.mainPressure + "<- mainPressure " + this.mainTempMin
				+ "<- mainTempMin " + this.mainTempMax + "<- mainTempMax "
				+ this.windSpeed + "<- windSpeed " + this.cloudsAll
				+ "<- cloudsAll " + this.weatherId + "<- weatherId "
				+ this.weatherMain + "<- weatherMain " + this.weatherDesc
				+ "<- weatherDesc " + this.weatherIcon + "<- weatherIcon "
				+ this.rain3h + "<- rain3h " + this.rain1h + "<- rain1h ");
	}
}
