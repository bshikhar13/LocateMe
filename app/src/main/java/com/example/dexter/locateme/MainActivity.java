package com.example.dexter.locateme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dexter.locateme.app.AppController;
import com.example.dexter.locateme.com.google.zxing.integration.android.IntentIntegrator;
import com.example.dexter.locateme.com.google.zxing.integration.android.IntentResult;
import com.example.dexter.locateme.utils.MySingleton;
import com.example.dexter.locateme.utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "SBTAG";
    private PrefManager pref;
    public ImageView imageView;
    public Button main_to_map;
    public TextView mainTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgtest);
        main_to_map = (Button)findViewById(R.id.main_to_map);
        main_to_map.setOnClickListener(this);
        mainTextView = (TextView)findViewById(R.id.mainTextView);
        pref = new PrefManager(getApplicationContext());
        if(pref.getDescription()!=null){
            mainTextView.setText(pref.getDescription());
        }

        if(pref.hasData()){
            try{
                File f = new File("/sdcard/"+pref.getMap());
                Log.i("SHWETA","/sdcard/"+pref.getMap());
                Bitmap bmp = BitmapFactory.decodeFile("/sdcard/" + pref.getMap());
                imageView.setImageBitmap(bmp);
            }catch (Exception e){
                e.printStackTrace();
            }


        }else{

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        switch (id) {
            case R.id.action_settings:
                // About option clicked.
                return true;
            case R.id.action_qr:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();

                // Exit option clicked.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            if(scanFormat!="QR_CODE"){
                String tag_json_obj = "json_obj_req";
                final String url = scanContent;


                final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage("Loading...");
                pDialog.show();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    String ap1 = response.getString("ap1");
                                    String ap2 = response.getString("ap2");
                                    String ap3 = response.getString("ap3");
                                    //String ap4 = response.getString("ap4");
                                    String ap4 = "Blank";
                                    String description = response.getString("description");
                                    String imageurl = response.getString("image_url");
                                    String venue_id = response.getString("venue_id");
                                    pref.SaveQrData(ap1,ap2,ap3,ap4,description,venue_id);

                                    String finalBaseUrl = null;
                                    try {
                                        URL baseurl = new URL(url);
                                        finalBaseUrl = baseurl.getHost();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    String imageUrl = finalBaseUrl+":8080/getimage/"+imageurl;
                                    fetchImage(imageUrl);
//

                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            description, Toast.LENGTH_LONG);
                                    toast.show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                pDialog.hide();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        // hide the progress dialog
                        pDialog.hide();
                    }
                });

                AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "This is not a QR Code!" + scanFormat, Toast.LENGTH_SHORT);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    void fetchImage(String imageUrl){

        Log.i(TAG, imageUrl);
        imageUrl = "http://" + imageUrl;
        //imageUrl = "http://i.imgur.com/7spzG.png";

        ImageRequest request = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap bitmap) {
                Log.i(TAG, "YOYO2");
                //ImageView imageView = (ImageView)findViewById(R.id.imgtest);
                //imageView.setImageBitmap(bitmap);
                File sdCardDirectory = Environment.getExternalStorageDirectory();
                String mapName = "locateme.png";
                File image = new File(sdCardDirectory, mapName);
                boolean success = false;
                FileOutputStream outStream;
                try {
                    outStream = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                    success = true;
                    pref.SaveMap(mapName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (success) {
                    Toast.makeText(getApplicationContext(), "Map saved with success",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error during Map saving", Toast.LENGTH_LONG).show();
                }
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG,"Error retrieving Map");
                    }
                });
        //MySingleton.getInstance(this).addToRequestQueue(request);
        try{
            AppController.getInstance().addToRequestQueue(request);

        }catch (Exception e){
            Log.i(TAG,e.toString() );
        }
        Log.i(TAG,imageUrl);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_to_map:
                startActivity(new Intent(this,MapActivity.class));
                break;
        }
    }
}