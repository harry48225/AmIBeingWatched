package xyz.deepseasev.harry.amibeingwatched;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by harry on 31/05/2017.
 */

public class popupImage extends Activity {

    private static String APIurl = "https://api.nasa.gov/planetary/earth/imagery?";

    private String date;

    private String Latitude;
    private String Longitude;

    private String APIKey;

    private ImageView image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the date from the extras


        Bundle extras = getIntent().getExtras();
        date = extras.getString("DATE");
        Log.i("INFO", "Date received " + date);

        Latitude = extras.getString("LAT");
        Longitude = extras.getString("LON");

        APIKey = extras.getString("APIKEY");

        setContentView(R.layout.popupimage);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);


        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.8));

        TextView topText = (TextView) findViewById(R.id.topText);
        topText.setText(date);

        image = (ImageView) findViewById(R.id.imageView);

        new GetImage().execute();

    }

    // Get the image from the NASA API
    class GetImage extends AsyncTask<Void, Void, Drawable> {

        @Override
        protected void onPreExecute() {

        }

        protected Drawable doInBackground(Void... urls) {
            String response;
            Drawable d = null;
            try {

                URL url = new URL(APIurl + "lat=" + Latitude + "&lon=" + Longitude + "&date=" + date + "&api_key=" + APIKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    response = stringBuilder.toString();
                }


                finally{
                    urlConnection.disconnect();
                }

            }

            catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

            if(response == null) {
                response = "an error occurred";
            }
            Log.i("INFO", response);

            String imageURL = null;
            try {
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                imageURL = object.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("INFO", "Image URL = " + imageURL);

            try {

                Log.i("INFO", "Getting Image");
                InputStream is = (InputStream) new URL(imageURL).getContent();

                Log.i("INFO", "Got Image");
                d = Drawable.createFromStream(is, "src name");


            } catch (MalformedURLException e) {

                Log.e("ERROR", "MalFormedURLException");
            } catch (IOException e) {
                Log.e("ERROR", "IOException");
            }
            return d;
        }

        protected void onPostExecute(Drawable d) {

            if (d == null) {
                Log.e("ERROR", "drawable d is null");
            } else {
                image.setImageDrawable(d);
            }


        }

    }

}


