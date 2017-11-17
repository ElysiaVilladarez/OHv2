package com.oh.ohv2.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by elysi on 10/29/2017.
 */

public class PermissionIntent {
    private Activity act;

    public PermissionIntent(Activity act){
        this.act = act;
    }
    public void changePermissionSettings(){
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", act.getPackageName(), null);
        i.setData(uri);
        act.startActivity(i);
    }
    public void changeLocationSettings(){
        act.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
