package thijsb.nrcmeestgelezen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private JSONObject jsonObject;
    private Integer currentArticle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // set webview client and settings
        WebView myWebView = (WebView) this.findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyWebViewClient()); // set the WebViewClient
        WebSettings ws = myWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        // dont store cookies
        ws.setSaveFormData(false);
        CookieManager.getInstance().setAcceptCookie(false);

        try {
            //check if wifi/3g active
            if(!isConnected(this)) buildDialog(this).show();
            else {
                //retrieve data
                jsonObject = new retrieveData().execute("https://www.nrc.nl/local-bigboard-data").get();

                //load article
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    loadArticleByTitle(extras.getString("articleTitle"));
                } else {
                    loadArticle(0);
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void loadArticle(Integer i) {
        WebView w = (WebView) this.findViewById(R.id.webview);
        w.loadUrl("https://www.nrc.nl" + getArticlePath(i));
        currentArticle = i;
        setTitle("[" + (i + 1) + "] " + getArticleTitle(i));
    }
    public void loadArticleByTitle(String title) { //iterate through pages and get one with title
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                if (title.equals(Html.fromHtml(row.getString("title")).toString())) {
                    loadArticle(i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String getArticlePath(Integer i) { //gets path
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            JSONObject article = jsonArray.getJSONObject(i);
            return article.getString("path");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "/";
    }
    public String getArticleTitle(Integer i) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            JSONObject article = jsonArray.getJSONObject(i);
            return (Html.fromHtml(article.getString("title"))).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "/";
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
                    loadArticle(currentArticle + 1);
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
                    loadArticle(currentArticle - 1);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.listview) {
            Intent myIntent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(myIntent);
            finish();
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


