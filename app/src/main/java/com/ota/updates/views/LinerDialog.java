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

public class LinerDialog {
    private final Dialog dialog;
    private Button dialogCancelBtn, dialogOkBtn, dialogMiddleBtn;
    private boolean isCancelable, haveMiddleBtn;

    @SuppressLint("SetTextI18n")
    public LinerDialog(final Context context, String title, String text, boolean haveMiddleBtn, boolean isCancelable){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.isCancelable = isCancelable;
        this.haveMiddleBtn = haveMiddleBtn;
        dialog.setContentView(R.layout.liner_dialog);
        TextView liner_dialogTitle = dialog.findViewById(R.id.liner_dialog_title);
        TextView liner_dialogText = dialog.findViewById(R.id.liner_dialog_text);

        if(haveMiddleBtn){
            dialogMiddleBtn = dialog.findViewById(R.id.liner_middle_button);
            dialogMiddleBtn.setVisibility(View.VISIBLE);
        }
        if(isCancelable){
            dialogCancelBtn = dialog.findViewById(R.id.liner_cancel_button);
            dialogCancelBtn.setVisibility(View.VISIBLE);
        }

        dialogOkBtn = dialog.findViewById(R.id.liner_ok_button);
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
