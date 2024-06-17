package com.hongyao.hyupdater.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;
/*
created by zhangrui for 20240613
*/



public  class HyDialog3Button extends Dialog implements View.OnClickListener {

    private WindowManager wm;
    private TextView dialog_okButton;
    private TextView dialog_cancelButton;
    private TextView dialog_deleteButton;
    private String dialogTitle;
    private String dialogMsg;
    private String dialog_ok;
    private TextView dialog_title;
    private TextView dialog_msg;
    public HyDialog3ButtonInterface hyDialog3ButtonInterface;
    private String type;
    public HyDialog3Button(Context context, String dialog_title, String dialog_msg,String dialog_ok,HyDialog3ButtonInterface hyDialog3ButtonInterface,String type) {
        super(context);
        this.hyDialog3ButtonInterface = hyDialog3ButtonInterface;
        this.dialog_ok = dialog_ok;
        this.dialogTitle = dialog_title;
        this.dialogMsg = dialog_msg;
        this.type = type;
        init(context);
        adjustWH();
    }

    private int mWidth = 0;
    private int mHeight = 0;
    private void adjustWH(){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        if(wm == null){
            wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        }
        mWidth = wm.getDefaultDisplay().getWidth();
        mHeight = wm.getDefaultDisplay().getHeight();
        if(mWidth >= mHeight){
            mWidth = mWidth / 3;
            mHeight -= 100;
        }else {
            mWidth -= 100;
            mHeight = mHeight / 3;
        }
        params.width = mWidth;
        params.height = mHeight;
        getWindow().setAttributes(params);
    }

    private void init(Context context) {
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.view_dialog_3button);
        dialog_title = findViewById(R.id.dialog_title);
        dialog_msg = findViewById(R.id.dialog_msg);
        dialog_okButton = findViewById(R.id.dialog_okButton);
        dialog_cancelButton = findViewById(R.id.dialog_cancelButton);
        dialog_deleteButton = findViewById(R.id.dialog_deleteButton);
        dialog_title.setText(this.dialogTitle);
        dialog_msg.setText(this.dialogMsg);
        //属性控制文本大小
        String buttonTextSize = SystemPropertiesUtils.getProperty("ro.ota.dialog.button.textSize", "-1");
        String titleTextSize = SystemPropertiesUtils.getProperty("ro.ota.dialog.title.textSize", "-1");
        String msgTextSize = SystemPropertiesUtils.getProperty("ro.ota.dialog.msg.textSize", "-1");
        if(!buttonTextSize.equals("-1")){
            dialog_cancelButton.setTextSize(Float.parseFloat(buttonTextSize));
            dialog_okButton.setTextSize(Float.parseFloat(buttonTextSize));
        }
        if(!titleTextSize.equals("-1")){
            dialog_title.setTextSize(Float.parseFloat(titleTextSize));
        }
        if(!msgTextSize.equals("-1")){
            dialog_msg.setTextSize(Float.parseFloat(msgTextSize));
        }
        dialog_okButton.setText(dialog_ok);
        dialog_title.setText(this.dialogTitle);
        dialog_msg.setText(this.dialogMsg);
        dialog_okButton.setOnClickListener(this);
        dialog_cancelButton.setOnClickListener(this);
        dialog_deleteButton.setOnClickListener(this);
        dialog_okButton.requestFocus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_okButton:
                switch (type){
                    case Contain.OPERATOR_3_NOMAL:
                        hyDialog3ButtonInterface.do3_dialog_okButton();
                        break;
                    case Contain.OPERATOR_3_ALREADYHAVEPACKAGE:
                        hyDialog3ButtonInterface.do3_dialog_installPackage();
                        break;
                }
                break;
            case R.id.dialog_cancelButton:
                break;
            case R.id.dialog_deleteButton:
                switch (type){
                case Contain.OPERATOR_3_NOMAL:
                    hyDialog3ButtonInterface.do3_dialog_deleteButton();
                    break;
                case Contain.OPERATOR_3_ALREADYHAVEPACKAGE:
                    hyDialog3ButtonInterface.do3_dialog_deleteFile();
                    break;
                }
                break;
        }
        if(isShowing()) dismiss();
    }
    public interface HyDialog3ButtonInterface{
        void do3_dialog_okButton();
        void do3_dialog_deleteButton();
        void do3_dialog_deleteFile();
        void do3_dialog_installPackage();
    }
}
