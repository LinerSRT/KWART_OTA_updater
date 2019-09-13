package com.ota.updates.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.ota.updates.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Objects;
import static com.ota.updates.utils.Config.*;

public class LinerProgressDialog {
    private final Dialog dialog;
    private Context context;
    private TextView dialogText;
    private AVLoadingIndicatorView loadView;
    private int timeout;

    @SuppressLint("SetTextI18n")
    public LinerProgressDialog(final Context context, String progressText, int timeout){
        this.context = context;
        this.timeout = timeout;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(AMOLED_VERSION) {
            dialog.setContentView(R.layout.checking_layout_amoled);
        } else {
            dialog.setContentView(R.layout.checking_layout);
        }
        dialogText = (TextView) dialog.findViewById(R.id.progress_dialog_text);
        loadView = (AVLoadingIndicatorView) dialog.findViewById(R.id.progress_dialog_bar);
        dialogText.setText(progressText);
    }
    public void show(){
        loadView.show();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }
    public void close(){

        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                loadView.hide();
                dialog.dismiss();
                Intent intent = new Intent(MANIFEST_LOADED);
                context.sendBroadcast(intent);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+timeout);
        handler.postDelayed(runnable, timeout);
    }

}
