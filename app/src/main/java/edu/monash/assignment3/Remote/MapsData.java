package edu.monash.assignment3.Remote;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Resturant;

public class MapsData extends AsyncTask<String,Void,String>{

    public static LatLng orderLatLng;

    public static ArrayList<LatLng> shopLatlngs;

    public static LatLng yourLatlng;

    FirebaseDatabase database;
    DatabaseReference reference;

    private ArrayList<Resturant> resturants;



    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Resturant");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                resturants.add(dataSnapshot.child("Resturant").getValue(Resturant.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected String doInBackground(String... strings) {

        orderLatLng = getOrderLatLng(Current.currentRequest.getAddress());
        for(int i=0;i<resturants.size();i++){
            String address = resturants.get(i).getAddress();
            shopLatlngs.add(getOrderLatLng(address));

        }



        return null;
    }

    private LatLng getOrderLatLng(String address){
        final String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+address.replace(" ","+")+"&key=AIzaSyC-MfbPD0PDgtepwWEBKT4amK-WXlpYN3E";
        String result = getLatLongByURL(url);

        LatLng mOrderLatlng = null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            double lon = Double.parseDouble(((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getString("lng"));
            double lat = Double.parseDouble(((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getString("lat"));
            mOrderLatlng = new LatLng(lat,lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mOrderLatlng;
    }





}
