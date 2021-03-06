package com.ensaitechnomobile.web.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ensai.appli.R;

public class Mails extends ActionBarActivity {
	/** Called when the activity is first created. */
	// @Override
	private WebView webview;
	private String id, pass;
	private SharedPreferences preferences;
	private ProgressBar progressBar;
	protected static final String TAG = "MAIL::";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the XML layout
		setContentView(R.layout.activity_web_view);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		progressBar = (ProgressBar) findViewById(R.id.web_view_progress);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		progressBar.setBackgroundResource(preferences.getInt("WEB_COLOR",
				R.drawable.backmotif_blue));

		// R�cup�ration de l'ID et du mot de passe dans les pr�f�rences
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		id = preferences.getString("login", "");
		pass = preferences.getString("password", "");

		myClickHandler();
	}

	// To handle "Back" key press event for WebView to go back to previous
	// screen.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		if (isConnected && webview != null) {
			if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
				webview.goBack();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		} else {
			return false;
		}
	}

	// When user clicks button, calls AsyncTask.
	// Before attempting to fetch the URL, makes sure that there is a network
	// connection.
	private void myClickHandler() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		if (isConnected) {

			webview = (WebView) findViewById(R.id.webview01);

			// Enable JavaScript and lets the browser go back
			webview.getSettings().setJavaScriptEnabled(true);
			webview.canGoBack();
			webview.getSettings().setSupportZoom(true);
			webview.getSettings().setBuiltInZoomControls(true);
			webview.setInitialScale(100);
			webview.getSettings().setLoadWithOverviewMode(true);
			webview.getSettings().setUseWideViewPort(true);

			webview.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}

				@Override
				public void onLoadResource(WebView view, String url) {
					webview.setEnabled(false);
				}

				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					progressBar.setVisibility(View.VISIBLE);
					webview.setEnabled(false);
				}

				@SuppressLint("NewApi")
				// Car on teste la version
				@Override
				public void onPageFinished(WebView view, String url) {
					 progressBar.setVisibility(View.GONE);
					webview.setEnabled(true);
					if (android.os.Build.VERSION.SDK_INT >= 19) {
						webview.evaluateJavascript(
								"document.getElementsByName('username')[0].value = '"
										+ id + "'", null);
						webview.evaluateJavascript(
								"document.getElementsByName('password')[0].value = '"
										+ pass + "'", null);
						webview.evaluateJavascript(
								"document.getElementByName('submit').click()",
								null);
					} else {
						// Avant API 19
						webview.loadUrl("javascript:document.getElementsByName('username')[0].value = '"
								+ id + "'");
						webview.loadUrl("javascript:document.getElementsByName('password')[0].value = '"
								+ pass + "'");
						webview.loadUrl("javascript:document.getElementByName('submit').click()");
					}
				}

				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					if (view.canGoBack()) {
						view.goBack();
					}
					Log.d(TAG, "webview erreur" + errorCode);
					Toast toast = Toast.makeText(getBaseContext(), description,
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
					toast.show();
				}
			});

			webview.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress) {
					progressBar.setProgress(progress);
				}
			});

			// The URL that webview is loading
			// webview.loadUrl("https://webmail.ensai.fr/SOGo/so/" + id
			// + "/Mail/view");

			webview.loadUrl(getString(R.string.webviewmail_URL));
		} else {
			Toast.makeText(this,
					getString(R.string.webview_internet_conection_error),
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	// Impl�mentation du menu

	/**
	 * M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du
	 * t�l�phone
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.action_bar_web_view, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 
	 */
	// M�thode qui se d�clenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une
		// action
		if (item.getItemId() == R.id.action_bar_webview) {
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			myClickHandler();
			return true;
		} else
			return false;
	}
}
