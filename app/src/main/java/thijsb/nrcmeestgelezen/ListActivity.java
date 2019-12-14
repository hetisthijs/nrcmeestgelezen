package thijsb.nrcmeestgelezen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import thijsb.nrcmeestgelezen.imageutils.ImageLoader;

public class ListActivity extends AppCompatActivity {

    private ListView list;
    private SharedPreferences sharedPref;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTitle("Meest gelezen");
        RequestWriteStorage();
        CreateList();
    }

    public void RequestWriteStorage() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    public void CreateList() {
        try {
            final List<Article> articles = new retrieveData().execute().get();

            // load list
            list = (ListView)findViewById(R.id.listView1);
            LazyAdapter adapter = new LazyAdapter(this, articles);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView)view.findViewById(R.id.text);
                    textView.setTextColor(getResources().getColor(R.color.read));

                    Intent myIntent = new Intent(ListActivity.this, MainActivity.class);
                    myIntent.putExtra("articlePosition", position);
                    startActivity(myIntent);
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class LazyAdapter extends BaseAdapter {
        private Activity activity;
        private List<Article> articles;
        private LayoutInflater inflater = null;
        public ImageLoader imageLoader;

        public LazyAdapter(Activity a, List<Article> articles) {
            this.activity = a;
            this.articles = articles;
            this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.imageLoader = new ImageLoader(activity.getApplicationContext());
        }

        public int getCount() { return articles.size()-1; }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if(convertView == null)
                vi = inflater.inflate(R.layout.list_single, null);

            Article article = articles.get(position);
            String title = article.getTitle();
            String image = article.getImage();

            TextView textv = (TextView)vi.findViewById(R.id.text);
            ImageView imagev = (ImageView)vi.findViewById(R.id.image);
            textv.setText(title);
            imageLoader.DisplayImage(image, imagev);

            if (sharedPref.contains(title)) { //if title exist, item has already been read
                textv.setTextColor(getResources().getColor(R.color.read));
            } else {
                textv.setTextColor(getResources().getColor(R.color.notRead));
            }
            return vi;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            refreshButton();
        }
        if (id == R.id.mainview) {
            Intent myIntent = new Intent(ListActivity.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshButton() {
        int index = list.getFirstVisiblePosition();
        View v = list.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
        CreateList();
        list.setSelectionFromTop(index, top);
        Toast.makeText(ListActivity.this, "Ververst", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshButton();
                } else {
                    Toast.makeText(ListActivity.this, "App heeft geen toegang om naar externe opslag te schrijven. Daardoor kan het de afbeeldingen niet weergeven. Overweeg alsnog permissie te verlenen aub.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
}
