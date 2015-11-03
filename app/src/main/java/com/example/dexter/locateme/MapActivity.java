package com.example.dexter.locateme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qozix.tileview.TileView;

public class MapActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_map);
        TileView tileView = new TileView(this);
        //Change this to pref.getDimensions() after saving the dimension on QR scanning i PrefManager

        tileView.setSize(1600,1200);
        //Change the tile size to be dunamic according to the dimension and complexity of the map
        tileView.addDetailLevel(2f, "%d_%d.png", 128, 128);


        ImageView logo = new ImageView( this );
        logo.setImageResource(R.drawable.logo);

        tileView.addMarker(logo,240,240,-0.5f,-1.0f);

        setContentView(tileView);
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
