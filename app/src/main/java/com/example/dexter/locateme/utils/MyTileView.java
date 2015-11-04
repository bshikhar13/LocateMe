package com.example.dexter.locateme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.qozix.tileview.TileView;
import com.qozix.tileview.graphics.BitmapProvider;
import com.qozix.tileview.graphics.BitmapProviderAssets;
import com.qozix.tileview.tiles.Tile;

/**
 * Created by dexter on 11/4/2015.
 */
public class MyTileView extends TileView implements BitmapProvider{

    public  MyTileView (Context context){
        super(context);
    }

    public MyTileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Bitmap getBitmap(Tile tile, Context context) {
        tile.equals("as");
        return null;
    }
}
