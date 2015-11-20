package com.example.dexter.locateme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.dexter.locateme.utils.MySingleton;
import com.example.dexter.locateme.utils.PrefManager;
import com.qozix.tileview.TileView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.*;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "SBActivity";

    TileView tileView;
    ImageView logo;


    public void senddata(String venue_id,String ap_id,String rssi, String phone_mac){
        Log.i(TAG,"I am here");
        String url = "http://192.168.1.85:8080/LENdata";
        HashMap<String, String> params = new HashMap<>();
        params.put("venue_id", venue_id);
        params.put("ap_id", ap_id);
        params.put("rssi", rssi);
        params.put("phone_mac", phone_mac);
        Log.i("DATA",venue_id + ap_id + rssi + phone_mac);

        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        //To restrict multiple volley requests.
        req.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// add the request object to the queue to be executed
        //AppController.getInstance().addToRequestQueue(req);
        MySingleton.getInstance(this).addToRequestQueue(req);
    }

    public void sendDataForSync(String venue_id,String ap_id,String rssi, String phone_mac){
        Log.i(TAG,"I am here");
        String url = "http://192.168.1.85:8080/LENdataForSync";
        HashMap<String, String> params = new HashMap<>();
        params.put("venue_id", venue_id);
        params.put("ap_id", ap_id);
        params.put("rssi", rssi);
        params.put("phone_mac", phone_mac);
        Log.i("DATA", venue_id + ap_id + rssi + phone_mac);

        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        //To restrict multiple volley requests.
        req.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this).addToRequestQueue(req);
    }

    public void getLocation(String venue_id,String phone_mac){
        String url = "http://192.168.1.85:8080/GetLocation";
        HashMap<String, String> params = new HashMap<>();
        params.put("venue_id", venue_id);
        params.put("phone_mac", phone_mac);

        JsonObjectRequest req = new JsonObjectRequest(url,new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String x = response.getString("y");
                            String y = response.getString("x");
                            double X = Double.parseDouble(x);
                            double Y = Double.parseDouble(y);
                            Log.i("PAKISTAN",response.toString());
                            Log.i("PAKISTAN", String.valueOf(X));
                            try{
                                tileView.moveMarker(logo,X,Y);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            Log.i("STOPPP",response.toString());
                            Log.e("Response:%n %s", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error: ", error.getMessage());
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this).addToRequestQueue(req);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.mapSyncProgressBar);
        PrefManager pref = new PrefManager(getApplicationContext());
        //Change this to pref.getDimensions() after saving the dimension on QR scanning i PrefManager

        tileView = new TileView(this);
        logo = new ImageView(this);

        tileView.setSize(1920, 1080);
        //Change the tile size to be dynamic according to the dimension and complexity of the map
        tileView.addDetailLevel(1f, "%d_%d.png", 40, 40);
        logo.setImageResource(R.drawable.logo);
        tileView.addMarker(logo, 100, 100, -0.5f, -1.0f);




        final ArrayList<String> ap_list ;
        ap_list = pref.get_AP_List();
        Log.i(TAG,ap_list.toString());
        final String venue_id = pref.get_Venue_Id();

        //Wifi Scanning Starts Here

        //ProgressBar Implementation

        final ProgressDialog dialog = new ProgressDialog(MapActivity.this);
        dialog.setTitle("Syncing...");
        dialog.setMessage("Please stand still...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        long delayInMillis = 32000;
        Timer timerProgress = new Timer();
        timerProgress.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, delayInMillis);

        //ProgressBar Implementation Ends

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        final String phone_mac = info.getMacAddress();

        progressBar.setVisibility(View.VISIBLE);

        final int DELAY_BEFORE_START = 0;
        final int RATE = 1000;
        final int[] count = {29};
        final Timer timer = new Timer();
        final Timer timerUpdate = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (count[0]-- == 0) {
                    timer.cancel();
                }
                try {
                    int i;
                    WifiManager wifi1 = null;
                    List<ScanResult> results = null;
                    try{
                        wifi1 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wifi1.startScan();
                        results = wifi1.getScanResults();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    for (i = 0; i < 4; i++) {
                        try {


                            ScanResult result = results.get(i);
                            String ap_name = result.SSID;
                            Log.i(TAG, ap_name);
                            //ap_list.contains(ap_name) || ap_name=="TC 71G"
                            if (ap_list.contains(ap_name)) {
                                Log.i("TAGISTAG", ap_name);
                                int rssi = result.level;
                                String rssivalue = String.valueOf(rssi);
                                sendDataForSync(venue_id, ap_name, rssivalue, phone_mac);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("TAGG", "Its in Timer");
            }
        }, DELAY_BEFORE_START, RATE);

        //progress.hide();
        progressBar.setVisibility(View.GONE);
        setContentView(tileView);
        timerUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int i;
                try {

                    WifiManager wifi1;
                    List<ScanResult> results = null;
                    try{
                        wifi1 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wifi1.startScan();
                        results = wifi1.getScanResults();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    for (i = 0; i < 4; i++) {

                        try {
                            ScanResult result = results.get(i);
                            String ap_name = result.SSID;
                            Log.i(TAG, ap_name);
                            //ap_list.contains(ap_name) || ap_name=="TC 71G"
                            if (ap_list.contains(ap_name)) {
                                int rssi = result.level;
                                String rssivalue = String.valueOf(rssi);
                                senddata(venue_id, ap_name, rssivalue, phone_mac);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }


                    }
                    getLocation(venue_id,phone_mac);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Log.i("TAGG", "In the Second Timer");
            }

        }, 30000, 1000);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MapActivity.super.onBackPressed();
                    }
                }).create().show();


    }
}
