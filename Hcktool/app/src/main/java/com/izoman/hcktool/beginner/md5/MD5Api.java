package com.izoman.hcktool.beginner.md5;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.izoman.hcktool.beginner.md5.AsyncResponse;

public class MD5Api extends AsyncTask<Void, Void, String> {
    private AsyncResponse delegate;
    private String URL;

    public MD5Api(String hash, AsyncResponse delegate) {
        this.URL = "http://md5decrypt.net/Api/api.php?hash=" + hash + "&hash_type=md5&email=abdullahboy%40hotmail.com&code=0e629ca5f29a60fd";
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();
            return response.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
