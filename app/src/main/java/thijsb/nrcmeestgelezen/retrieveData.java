package thijsb.nrcmeestgelezen;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

class retrieveData extends AsyncTask<String, Void, JSONObject> {
    @Override
    protected JSONObject doInBackground(String... str) {
        URLConnection urlConn;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(str[0]);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            return new JSONObject(stringBuffer.toString());

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
}