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

public class OTADialog {
    private final Dialog dialog;
    private Context context;
    private int btn_type = 0;
    private Button dialogCancelBtn, dialogOkBtn, dialogMiddleBtn;
    private TextView dialogTitle, dialogText;

    @SuppressLint("SetTextI18n")
    public OTADialog(final Context context, String title, String text, int buttonSet, String cancel_text, String middle_text, String ok_text){
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ota_dialog);
        dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        dialogText = (TextView) dialog.findViewById(R.id.dialog_text);
        dialogCancelBtn = (Button) dialog.findViewById(R.id.cancel_button);
        dialogCancelBtn.setText(cancel_text);
        dialogOkBtn = (Button) dialog.findViewById(R.id.ok_button);
        dialogOkBtn.setText(ok_text);
        dialogMiddleBtn = (Button) dialog.findViewById(R.id.middle_button);
        dialogMiddleBtn.setText(middle_text);


        switch (buttonSet){
            case 001:
                dialogCancelBtn.setVisibility(View.GONE);
                dialogMiddleBtn.setVisibility(View.GONE);
                dialogOkBtn.setVisibility(View.VISIBLE);
                break;
            case 010:
                dialogCancelBtn.setVisibility(View.GONE);
                dialogMiddleBtn.setVisibility(View.VISIBLE);
                dialogOkBtn.setVisibility(View.GONE);
                break;
            case 100:
                dialogCancelBtn.setVisibility(View.VISIBLE);
                dialogMiddleBtn.setVisibility(View.GONE);
                dialogOkBtn.setVisibility(View.GONE);
                break;
            case 101:
                dialogCancelBtn.setVisibility(View.VISIBLE);
                dialogMiddleBtn.setVisibility(View.GONE);
                dialogOkBtn.setVisibility(View.VISIBLE);
                break;
            case 111:
                dialogCancelBtn.setVisibility(View.VISIBLE);
                dialogMiddleBtn.setVisibility(View.VISIBLE);
                dialogOkBtn.setVisibility(View.VISIBLE);
                break;
        }

        dialogTitle.setText(title);
        dialogText.setText(text);
    }
    public void show(){
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }
    public void close(){
        dialog.dismiss();
    }

    public void setNegativeBtn(View.OnClickListener onClickListener){
        dialogCancelBtn.setOnClickListener(onClickListener);
    }
    public void setNeutralBtn(View.OnClickListener onClickListener){
        dialogMiddleBtn.setOnClickListener(onClickListener);
    }
    public void setOkBtn(View.OnClickListener onClickListener){
        dialogOkBtn.setOnClickListener(onClickListener);
    }

    public void setDialogText(String text) {
        this.dialogText.setText(text);
    }

    public void setDialogTitle(String text) {
        this.dialogTitle.setText(text);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public Button getDialogOkBtn() {
        return dialogOkBtn;
    }

    public Button getDialogCancelBtn() {
        return dialogCancelBtn;
    }

    public TextView getDialogText() {
        return dialogText;
    }

    public TextView getDialogTitle() {
        return dialogTitle;
    }
}
