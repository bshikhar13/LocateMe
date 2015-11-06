package com.example.dexter.locateme;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.dexter.locateme.app.AppController;
import com.example.dexter.locateme.utils.PrefManager;
import com.qozix.tileview.TileView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.os.*;
import android.widget.ProgressBar;

public class MapActivity extends AppCompatActivity {

    public ProgressBar progressBar;

    private static final String TAG = "SBActivity";
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    private PrefManager pref;

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public void senddata(String venue_id,String ap_id,String rssi, String phone_mac){
        Log.i(TAG,"I am here");
        String url = "http://192.168.1.6:8080/LENdata";
        HashMap<String, String> params = new HashMap<String, String>();
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

// add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }

    public void sendDataForSync(String venue_id,String ap_id,String rssi, String phone_mac){
        Log.i(TAG,"I am here");
        String url = "http://192.168.1.6:8080/LENdataForSync";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("venue_id", venue_id);
        params.put("ap_id", ap_id);
        params.put("rssi", rssi);
        params.put("phone_mac", phone_mac);
        Log.i("DATA", venue_id + ap_id + rssi + phone_mac);
        Log.i(TAG,"I will go to dubai");
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

// add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ProgressDialog progress = new ProgressDialog(this);

        progress.setMessage("Syncing...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);


        pref = new PrefManager(getApplicationContext());
        //setContentView(R.layout.activity_map);
        TileView tileView = new TileView(this);
        //Change this to pref.getDimensions() after saving the dimension on QR scanning i PrefManager

        tileView.setSize(1920, 1080);
        //Change the tile size to be dunamic according to the dimension and complexity of the map
        tileView.addDetailLevel(1f, "%d_%d.png", 40, 40);
        ImageView logo = new ImageView( this );
        logo.setImageResource(R.drawable.logo);
        tileView.addMarker(logo, 100, 100, -0.5f, -1.0f);
        setContentView(tileView);

        final ArrayList<String> ap_list ;
        ap_list = pref.get_AP_List();
        Log.i(TAG,ap_list.toString());
        final String venue_id = pref.get_Venue_Id();

        //Wifi Scanning Starts Here


        /* Issues :
        1.The loop is running 30 times but enteries in the sync table are more than 30.
            Need to fix this issue. The later thread is running as expected.
        2.The Progressbar is not Showing up
        */
        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        final String phone_mac = info.getMacAddress();

        final Handler h = new Handler();
        final int delay = 1000; //milliseconds

        progress.show();


        final int DELAY_BEFORE_START = 0;
        final int RATE = 1000;
        final int[] count = {29};
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(count[0]-- ==0){
                    timer.cancel();
                }
                try{
                    int i=0;
                    for(i=0;i<4;i++){
                        try{
                            ScanResult result = wifi.getScanResults().get(i);
                            String ap_name = result.SSID;
                            Log.i(TAG, ap_name);
                            //ap_list.contains(ap_name) || ap_name=="TC 71G"
                            if(ap_name.equals("TC 71G")){
                                Log.i("TAGISTAG",ap_name);
                                int rssi = result.level;
                                String rssivalue = String.valueOf(rssi);
                                sendDataForSync(venue_id, ap_name, rssivalue, phone_mac);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.i("TAG", "Fuck BC");
            }
        }, DELAY_BEFORE_START, RATE);

        progress.hide();

        h.postDelayed(new Runnable() {
            public void run() {
                //itemsAdapter.clear();
                int i = 0;
                for (i = 0; i < 4; i++) {
                    ScanResult result;
                    try {
                        result = wifi.getScanResults().get(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    String ap_name = result.SSID;
                    Log.i(TAG, ap_name);
                    //ap_list.contains(ap_name) || ap_name=="TC 71G"
                    if (ap_list.contains(ap_name)) {
                        int rssi = result.level;
                        String rssivalue = String.valueOf(rssi);
                        senddata(venue_id, ap_name, rssivalue, phone_mac);
                    }

                }
                h.postDelayed(this, delay);
            }
        }, delay);


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
}
