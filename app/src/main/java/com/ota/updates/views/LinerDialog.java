package com.ota.updates.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ota.updates.R;

import java.util.Objects;

import static com.ota.updates.utils.Config.AMOLED_VERSION;

public class LinerDialog {
    private final Dialog dialog;
    private Context context;
    private Button dialogCancelBtn, dialogOkBtn, dialogMiddleBtn;
    private TextView liner_dialogTitle, liner_dialogText;
    private boolean isCancelable, haveMiddleBtn;

    @SuppressLint("SetTextI18n")
    public LinerDialog(final Context context, String title, String text, boolean haveMiddleBtn, boolean isCancelable){
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.isCancelable = isCancelable;
        this.haveMiddleBtn = haveMiddleBtn;
        if(AMOLED_VERSION) {
            dialog.setContentView(R.layout.liner_dialog_amoled);
        } else {
            dialog.setContentView(R.layout.liner_dialog);
        }
        liner_dialogTitle = (TextView) dialog.findViewById(R.id.liner_dialog_title);
        liner_dialogText = (TextView) dialog.findViewById(R.id.liner_dialog_text);

        if(haveMiddleBtn){
            dialogMiddleBtn = (Button) dialog.findViewById(R.id.liner_middle_button);
            dialogMiddleBtn.setVisibility(View.VISIBLE);
        }
        if(isCancelable){
            dialogCancelBtn = (Button) dialog.findViewById(R.id.liner_cancel_button);
            dialogCancelBtn.setVisibility(View.VISIBLE);
        }

        dialogOkBtn = (Button) dialog.findViewById(R.id.liner_ok_button);
        dialogOkBtn.setVisibility(View.VISIBLE);

        liner_dialogTitle.setText(title);
        liner_dialogText.setText(text);
    }
    public void show(){
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void close(){
        dialog.dismiss();
    }

    public void setupCancelBtn(String name, View.OnClickListener onClickListener){
        if(isCancelable){
            dialogCancelBtn.setText(name);
            dialogCancelBtn.setOnClickListener(onClickListener);
        }
    }

    public void setupMiddleBtn(String name, View.OnClickListener onClickListener){
        if(haveMiddleBtn){
            dialogMiddleBtn.setText(name);
            dialogMiddleBtn.setOnClickListener(onClickListener);
        }
    }

    public void setupOkBtn(String name, View.OnClickListener onClickListener){
        dialogOkBtn.setText(name);
        dialogOkBtn.setOnClickListener(onClickListener);
    }


}
