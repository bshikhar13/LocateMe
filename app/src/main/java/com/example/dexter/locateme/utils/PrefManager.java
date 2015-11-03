package com.example.dexter.locateme.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by dexter on 10/30/2015.
 */
public class PrefManager {

    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "LocateMeSP";

    // All Shared Preferences Keys
    private static final String AP1 = "ap1";
    private static final String AP2 = "ap2";
    private static final String AP3 = "ap3";
    private static final String AP4 = "ap4";
    private static final String DESCRIPTION = "description";
    private static final String MAP = "mapLocation";



    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void SaveQrData(String ap1, String ap2,String ap3, String ap4, String description) {
        editor.clear();
        editor.commit();
        editor.putString(AP1, ap1);
        editor.putString(AP2, ap2);
        editor.putString(AP3, ap3);
        editor.putString(AP4, ap4);
        editor.putString(DESCRIPTION, description);
        editor.commit();
        Log.i("YOYO", "Data is saved");
    }

    public void SaveMap(String mapLocation){

        editor.putString(MAP,mapLocation);
        editor.commit();
    }

    public boolean hasData(){
        if(pref.getString(MAP, null) == null){
            return false;
        }else{
            return true;
        }
    }

    public String getMap(){
        return pref.getString(MAP,null);
    }

    public String getDescription(){
        Log.i("YOYO","I am here" + pref.getString(DESCRIPTION,null));
        return pref.getString(DESCRIPTION,null);

    }

//    public void createLogin(String username, String name, int age, String gender, String phone, String token) {
//        editor.putString(KEY_USERNAME, username);
//        editor.putString(KEY_NAME, name);
//        editor.putInt(KEY_AGE, age);
//        editor.putString(KEY_GENDER, gender);
//        editor.putString(KEY_PHONE, phone);
//        editor.putBoolean(KEY_IS_LOGGED_IN, true);
//        editor.putString(KEY_TOKEN,token);
//        editor.commit();
//    }


}
