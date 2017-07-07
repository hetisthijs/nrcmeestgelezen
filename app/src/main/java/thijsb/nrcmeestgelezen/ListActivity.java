package thijsb.nrcmeestgelezen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import thijsb.nrcmeestgelezen.imageutils.ImageLoader;

public class ListActivity extends AppCompatActivity {

    private ListView list;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTitle("Meest gelezen");
        CreateList();
    }

    public void CreateList() {
        final List<String> text = new ArrayList<>();
        List<String> images = new ArrayList<>();
        try {
            //get all images and titles
            JSONObject jsonObject = new retrieveData().execute("https://www.nrc.nl/local-bigboard-data").get();
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                String title = Html.fromHtml(row.getString("title")).toString();
                String imageUrl = "";
                if (row.has("image")) {
                    imageUrl = row.getJSONObject("image").getJSONObject("versions").getString("xsmall");
                }
                if (title.equals("")) continue;
                text.add(title);
                images.add(imageUrl);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // load list
        list = (ListView)findViewById(R.id.listView1);
        LazyAdapter adapter = new LazyAdapter(this, images, text);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView)view.findViewById(R.id.text);
                textView.setTextColor(Color.GRAY);

                Intent myIntent = new Intent(ListActivity.this, MainActivity.class);
                myIntent.putExtra("articleTitle", text.get(position));
                startActivity(myIntent);
            }
        });
    }

    public class LazyAdapter extends BaseAdapter {
        private Activity activity;
        private List<String> images;
        private List<String> text;
        private LayoutInflater inflater = null;
        public ImageLoader imageLoader;

        public LazyAdapter(Activity a, List<String> images, List<String> text) {
            this.activity = a;
            this.images = images;
            this.text = text;
            this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.imageLoader = new ImageLoader(activity.getApplicationContext());
        }

        public int getCount() { return text.size()-1; }

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

            // set text and image
            TextView textv = (TextView)vi.findViewById(R.id.text);;
            ImageView imagev = (ImageView)vi.findViewById(R.id.image);
            textv.setText(text.get(position));

            if (sharedPref.contains(text.get(position))) { //if title exist, item has already been read
                textv.setTextColor(Color.GRAY);
            } else {
                textv.setTextColor(Color.BLACK);
            }

            imageLoader.DisplayImage(images.get(position), imagev);
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
            int index = list.getFirstVisiblePosition();
            View v = list.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
            CreateList();
            list.setSelectionFromTop(index, top);
            Toast.makeText(ListActivity.this, "Ververst", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.mainview) {
            Intent myIntent = new Intent(ListActivity.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
