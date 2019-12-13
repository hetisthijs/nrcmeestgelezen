package thijsb.nrcmeestgelezen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;
    private JSONObject jsonObject;
    private Integer currentArticle = 0;
    private String hideMenuItem = "";
    private String toShare = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        removeOldRead();

        // set webview client and settings
        WebView myWebView = (WebView) this.findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyWebViewClient()); // set the WebViewClient
        WebSettings ws = myWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        // dont store cookies
        //ws.setSaveFormData(false);
        //CookieManager.getInstance().removeAllCookie();
        //CookieSyncManager.getInstance().sync();
        //CookieManager.getInstance().setCookie(".nrc.nl", "nmt_closed_cookiebar=1");

        try {
            //check if wifi/3g active
            if(!isConnected(this)) buildDialog(this).show();
            else {
                //retrieve data
                jsonObject = new retrieveData().execute("https://www.nrc.nl/local-bigboard-data").get();

                //load article (chosen or first) in webview
                Bundle extras = getIntent().getExtras();
                if (extras != null && extras.containsKey("articleTitle") && extras.containsKey("articlePath") && extras.containsKey("articlePosition")) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true); // visible back button
                    hideMenuItem = "listview";  // invisible listview button

                    loadArticle(extras.getString("articleTitle"), extras.getString("articlePath"), extras.getInt("articlePosition"));
                } else {
                    loadArticleByPosition(0);
                    Toast.makeText(MainActivity.this, "Veeg naar links en rechts om te bladeren", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void loadArticle(String title, String path, Integer position) { //load article in webview based on json index number
        String url = "https://www.nrc.nl" + path;
        WebView w = (WebView) this.findViewById(R.id.webview);
        w.loadUrl(url);
        setRead(title, url);
        setTitle("[" + (position + 1) + "] " + title);
        currentArticle = position;
        toShare = title + " - " + url;
    }

    public void loadArticleByPosition(Integer position) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            JSONObject row = jsonArray.getJSONObject(position);
            String title = Html.fromHtml(row.getString("title")).toString();
            String path = row.getString("path");
            loadArticle(title, path, position);
        } catch (JSONException e) {
            buildDialog(this).show();
        }
    }

    // add sharedpreference pair with title|timestamp
    private void setRead(String title, String url) {
        Long tsLong = System.currentTimeMillis()/1000;
        String timestamp = tsLong.toString();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(title, timestamp);
        editor.apply();
    }

    // remove sharedpreference with timestamp older than 7 days
    private void removeOldRead() {
        Long tsLong = (System.currentTimeMillis()/1000)-604800000L;
        String timestamp_7days = tsLong.toString();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(entry.getValue().toString().matches("\\d+(?:\\.\\d+)?")) { //if number
                if (Integer.parseInt(entry.getValue().toString()) < Integer.parseInt(timestamp_7days)) {
                    sharedPref.edit().remove(entry.getKey()).apply();
                }
            }
        }
    }

    public class JavaScriptInterface {
        Context mContext;
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void swipeLeft() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadArticleByPosition(currentArticle + 1);
                }
            });
        }

        @JavascriptInterface
        public void swipeRight() {
            if (currentArticle == 0)
                return;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadArticleByPosition(currentArticle - 1);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        //listview option
        if(hideMenuItem.equals("listview")) {
            MenuItem listview = menu.findItem(R.id.listview);
            listview.setVisible(false);
        }

        /*
        //share button
        MenuItem shareItem = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.listview) {
            Intent myIntent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(myIntent);
            finish();
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, toShare);
            startActivity(Intent.createChooser(sharingIntent, "Deel artikel..."));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //check if internet connection
    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()));
        } else return false;
    }
    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Geen internetverbinding");
        builder.setMessage("Kan niet verbinden met NRC.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        return builder;
    }
}


