package edu.monash.assignment3;

import android.Manifest;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Request;
import edu.monash.assignment3.Model.Rider;
import edu.monash.assignment3.Remote.MapsData;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_PREMISSION_REQUEST = 1001;

    private Location mLastLocation;


    String requestId = "";

    private TextView mDuration;
    private TextView mDistance;


    //using googlemapapi
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1000;
    private static int  FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference request = database.getReference("Requests");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDuration = (TextView) findViewById(R.id.duration);
        mDistance = (TextView) findViewById(R.id.distance);


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestRuntimePermission();
        }
        else
        {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //get requet id
        requestId = getIntent().getStringExtra("RequestsId");



        displayLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestRuntimePermission();
        }
        else
        {

            //get currentlocation
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(mLastLocation != null){

                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                int lat = (int) latitude;
                int lon = (int) longitude;

                Rider rider = new Rider(lat,lon);


                //save to firebase
                request.child(requestId).child("Rider").setValue(rider);
                //draw the map
                drawRoute(lat,lon);


            }
            else
            {
                //Toast.makeText(this,"Could not get the location",Toast.LENGTH_SHORT).show();
                Log.d("DEBUG","Could not get the location");
            }
        }
    }

    private void drawRoute(double lat, double lon) {
        //final String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+Current.currentRequest.getAddress().replace(" ","+")+"&key=AIzaSyC-MfbPD0PDgtepwWEBKT4amK-WXlpYN3E";
        final String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+String.valueOf(lat)+","+String.valueOf(lon)+"&destination="+Current.currentRequest.getAddress().replace(" ","+")+"&key=AIzaSyBtR7Gaw1BedbEt_uENhWurcVyVJbSyyfw";

        GetLocations getLocations = new GetLocations();
        getLocations.execute(url);

    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Toast.makeText(this,"This device is not support",Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void requestRuntimePermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },LOCATION_PREMISSION_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PREMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();

                        displayLocation();
                    }
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
             mLastLocation = location;
             mMap.clear();
             displayLocation();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
            displayLocation();;
            startLocationUpdate();
    }

    private void startLocationUpdate() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.connect();
        }
    }



    private class GetLocations extends AsyncTask<String, Void, String> {


        @Override
            protected String doInBackground(String... strings) {
                String result = MapsData.getLatLongByURL(strings[0]);
                return result;
            }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            LatLng orderLatlon = null;
            LatLng driverLatlon = null;
            JSONObject jsonObject;
            JSONArray jsonArray;
            String distance = null;
            String duration = null;
            String[] directionsList = null;
            try {
                jsonObject = new JSONObject(result);
                duration = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
                distance = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                String endLatitude = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("end_location").getString("lat");
                String endLongitude = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("end_location").getString("lng");
                String startLatitude = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("start_location").getString("lat");
                String startLongitude = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("start_location").getString("lng");

                jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                directionsList = getPaths(jsonArray);
                orderLatlon = new LatLng(Double.parseDouble(endLatitude),Double.parseDouble(endLongitude));
                driverLatlon = new LatLng(Double.parseDouble(startLatitude),Double.parseDouble(startLongitude));


            } catch (JSONException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_home_black_24dp);

            MarkerOptions orderMarkerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker() ).title("Order of "+ Current.currentRequest.getPhone()).position(orderLatlon);
            MarkerOptions driverMarkerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker() ).title("Driver").position(driverLatlon);
            mMap.addMarker(orderMarkerOptions);
            mMap.addMarker(driverMarkerOptions);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLatlon));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

            mDistance.setText(distance);
            mDuration.setText(duration);

            displayDirection(directionsList);
        }
    }


    public void displayDirection(String[] directionsList)
    {

        int count = directionsList.length;
        for(int i = 0;i<count;i++)
        {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.RED);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }



    public String[] getPaths(JSONArray googleStepsJson)
    {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];

        for(int i = 0;i<count;i++)
        {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    public String getPath(JSONObject googlePathJson)
    {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }

}
