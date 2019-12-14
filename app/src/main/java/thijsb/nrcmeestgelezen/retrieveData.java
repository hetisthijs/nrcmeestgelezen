package thijsb.nrcmeestgelezen;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

class retrieveData extends AsyncTask<Void, Void, List> {
    String[] ignoreContaining = {"Sudoku", "In het midden", "NRC Handelsblad van", "Colofon"}; //ignore titles that contain one of these strings
    String bigboard = "https://www.nrc.nl/local-bigboard-data";

    @Override
    protected List doInBackground(Void... params) {
        URLConnection urlConn;
        BufferedReader bufferedReader = null;
        List<Article> articles = new ArrayList<>();

        try {
            URL url = new URL(bigboard);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            JSONObject jsonObject = new JSONObject(stringBuffer.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("pages");
            Integer position = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                String title = Html.fromHtml(row.getString("title")).toString();
                String path = row.getString("path");
                String image = "";
                if (row.has("image")) {
                    image = row.getJSONObject("image").getJSONObject("versions").getString("xsmall");
                }
                if (stringContainsItemFromList(title, ignoreContaining) || title.equals("")) continue;
                Article article = new Article(title, path, image, position);
                articles.add(article);
                position++;
            }

            return articles;

        } catch(Exception ex) {
            Log.e("App", "retrieveData", ex);
            return null;
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        for(int i =0; i < items.length; i++) {
            if(inputStr.contains(items[i])) {
                return true;
            }
        }
        return false;
    }
}