package xyz.deepseasev.harry.amibeingwatched;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText lat;
    private EditText lon;


    private static String APIurl = "https://api.nasa.gov/planetary/earth/assets?";
    private static String APIKey = "ElcYWXGgHpZ9WLvFI6v4g5cUQo1fhd58kYkYNtBp";
    private String Latitude;
    private String Longitude;

    private ListView lv;
    private ArrayAdapter<String> adapter;

    private ArrayList<String> values = new ArrayList<String>();

    private LocationManager lm;
    private Location location;

    private int permissionResult;

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Set up the entry or latitude and longitude
        lat = (EditText) findViewById(R.id.latEntry);
        lon = (EditText) findViewById(R.id.longEntry);

        // Set up list view to contain the dates
        lv = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        lv.setAdapter(adapter);

        // What to do when a button is pressed in the list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // A bit of logging
                Log.i("INFO", values.get(position) + " was Clicked!");

                // Remove the time from the date thing
                String date = values.get(position);

                date = date.substring(0, 10);
                Log.i("INFO", "Date is " + date);

                // Code to open a popup window with the picture

                Intent image = new Intent(MainActivity.this, popupImage.class);

                // Send this extra data to the intent
                image.putExtra("DATE", date);

                image.putExtra("LAT", Latitude);
                image.putExtra("LON", Longitude);

                image.putExtra("APIKEY", APIKey);

                startActivity(image);


            }
        });

        // Set up the goolge API client so that we can get the location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
    }

    // This is the function that handles what happens when the GPS button is pressed
    public void gpsButton(View view) {

        // Check if we have the location permissions, if we don't: get them
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, permissionResult);
            return;
        }

        // If we have the permission set the text of the lat and long boxes to the phone's lat and long
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            lat.setText(Latitude);
            lon.setText(Longitude);
        }




    }

    // This is the function that handles what happens when the submit button is pressed
    public void submitButton(View view) {

        // Get the latitude and longitude as strings from the entry boxes
        Latitude = lat.getText().toString();
        Longitude = lon.getText().toString();

        // Call the API caller
        new GetDates().execute();


    }

    // Do stuff when connected to the Google services
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (location != null) {
                Latitude = String.valueOf(location.getLatitude());
                Longitude = String.valueOf(location.getLongitude());

            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    // Get the dates from the NASA API
    class GetDates extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            values.clear();
            adapter.notifyDataSetChanged();
        }

        protected String doInBackground(Void... urls) {
            try {

                URL url = new URL(APIurl + "lat=" + Latitude + "&lon=" + Longitude + "&api_key=" + APIKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }

            }

            catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "an error occurred";
            }
            Log.i("INFO", response);

            JSONArray dates = null;
            try {
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                dates = object.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            int id;
            String date;
            for (int i=0; i < dates.length(); i++) {
                try {
                    JSONObject row = dates.getJSONObject(i);
                    date = row.getString("date");
                    values.add(date);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            adapter.notifyDataSetChanged();
        }

    }



}