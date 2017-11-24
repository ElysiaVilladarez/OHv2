package com.oohana.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by elysi on 10/29/2017.
 */

public class PermissionIntent {
    private Context c;

    public PermissionIntent(Context c){
        this.c = c;
    }

    public void changePermissionSettings(){
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", c.getPackageName(), null);
        i.setData(uri);
        c.startActivity(i);
    }
    public void changeLocationSettings(){
        c.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
