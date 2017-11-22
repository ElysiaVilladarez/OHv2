package com.oh.ohv2.helpers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.oh.ohv2.R;

/**
 * Created by SaperiumDev on 11/22/2017.
 */

public class DialogConfirmCreator {
    private Dialog alertDialogs;
    private TextView alertDialogMes;
    private Button alertOkButton, alertCancelButton;
    public DialogConfirmCreator(Context c){
        alertDialogs = new Dialog(c);
        alertDialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogs.setContentView(R.layout.dialog_confirm);
        alertDialogs.setCancelable(false);
        alertDialogs.setCanceledOnTouchOutside(false);
        alertDialogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogMes = (TextView)alertDialogs.findViewById(R.id.message);
        alertOkButton = (Button)alertDialogs.findViewById(R.id.ok_button);
        alertCancelButton = (Button)alertDialogs.findViewById(R.id.cancel_button);
    }
    public void setAlertDialogMes(String mes){
        alertDialogMes.setText(mes);
    }
    public void setAlertOkButtonClick(View.OnClickListener listener){
        alertOkButton.setOnClickListener(listener);
    }
    public void setAlertCancelButtonClick(View.OnClickListener listener){
        alertCancelButton.setOnClickListener(listener);
    }
    public void dismissDialog(){
        alertDialogs.dismiss();
    }
    public void showDialog(){
        alertDialogs.show();
    }
}
