package com.oohana.home;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.oohana.R;
import com.oohana.helpers.Constants;
import com.oohana.helpers.DialogConfirmCreator;
import com.oohana.helpers.PermissionIntent;

public class TransparentActDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transparent_act_dialog);

        Intent intent = getIntent();

        if(intent!=null){
            if(Constants.ACTION_GPS_OFF.equals(intent.getAction())){
                final DialogConfirmCreator dialog = new DialogConfirmCreator(TransparentActDialog.this);
                dialog.setAlertDialogMes("Test");
                dialog.setAlertOkButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(Constants.LOG_TAG_TRANS_DIALOG, "Direct user to settings . . .");
                        PermissionIntent pi = new PermissionIntent(TransparentActDialog.this);
                        dialog.dismissDialog();
                        pi.changeLocationSettings();
                        finishAffinity();
                    }
                });
                dialog.setAlertCancelButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(Constants.LOG_TAG_TRANS_DIALOG, "Stop geofencing . . .");
                        dialog.dismissDialog();
                        finishAffinity();
                    }
                });
                dialog.showDialog();
            }
        }
    }
}
