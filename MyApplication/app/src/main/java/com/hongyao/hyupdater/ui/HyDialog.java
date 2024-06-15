package com.hongyao.hyupdater.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;
import com.hongyao.hyupdater.view.activity.MainActivity;


public  class HyDialog extends Dialog implements View.OnClickListener {
    private TextView dialog_title;
    private TextView dialog_msg;
    private ProgressBar dialog_progress;
    private Button dialog_okButton;
    private Button dialog_cancelButton;
    private WindowManager wm;
    private Context mContext;
    private MyDialogInterface myDialogInterface;
    private String type;

    public HyDialog(Context context, String dialog_title,
                    String dialog_msg,String dialog_okButton,String dialog_cancelButton,int progressVisibility,MyDialogInterface myDialogInterface,String type) {
        super(context);
        this.type = type;
        this.myDialogInterface = myDialogInterface;
        mContext = context;
        init(context,dialog_title, dialog_msg,dialog_okButton,dialog_cancelButton,progressVisibility);
        adjustWH();
    }
    private void init(Context context,String dialog_title1, String dialog_msg1,String dialog_okButton1,String dialog_cancelButton1,int progressVisibility) {
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.view_dialog);
        dialog_title = (TextView)findViewById(R.id.dialog_title);
        dialog_msg = (TextView)findViewById(R.id.dialog_msg);
        dialog_progress = (ProgressBar) findViewById(R.id.dialog_progress);
        dialog_okButton = (Button) findViewById(R.id.dialog_okButton);
        dialog_cancelButton = (Button) findViewById(R.id.dialog_cancelButton);
        dialog_title.setText(dialog_title1);
        dialog_msg.setText(dialog_msg1);
        dialog_okButton.setText(dialog_okButton1);
        dialog_cancelButton.setText(dialog_cancelButton1);
        dialog_progress.setVisibility(progressVisibility);
        dialog_okButton.setOnClickListener(this);
        dialog_cancelButton.setOnClickListener(this);
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
        if(Contain.OPERATOR_DOWNLODINGPROGESS.equals(type)){
            dialog_okButton.setVisibility(View.GONE);
        }

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
            mHeight -= 50;
        }else {
            mWidth -= 50;
            mHeight = mHeight / 3;
        }
        params.width = mWidth;
        params.height = mHeight;
        getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_okButton:
                switch (type){
                    case Contain.OPERATOR_BACK:
                        myDialogInterface.closeApp();
                        break;
                    case Contain.OPERATOR_DOWNLODINGPROGESS:
                        myDialogInterface.downloadingProgress();
                        break;
                    case Contain.OPERATOR_DOWNLOAD:
                        myDialogInterface.downloadUpdate();
                        break;
                    case Contain.OPERATOR_INSTALLPACKAGE:
                        myDialogInterface.installPackage();
                        break;
                    case Contain.OPERATOR_QUERY_STATE:
                        SystemPropertiesUtils.setProperty(Contain.ISQUERY_SHOW,"false");
                        Intent intenter = new Intent(mContext, MainActivity.class);
                        intenter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intenter);
                        break;
                }
                break;
            case R.id.dialog_cancelButton:
                switch (type){
                    case Contain.OPERATOR_DOWNLODINGPROGESS:
                        myDialogInterface.downloadingProgressCancel();
                        break;
                    case Contain.OPERATOR_QUERY_STATE:
                        //SystemPropertiesUtils.setProperty(Contain.ISQUERY_SHOW,"false");
                        break;
                }
                break;
        }
        if(isShowing()) dismiss();
    }
    public interface MyDialogInterface{
        void closeApp();
        void downloadingProgress();
        void downloadingProgressCancel();
        void downloadUpdate();
        void installPackage();
    }
    public void setProgressBar(int progress){
        if(dialog_progress != null){
            dialog_progress.setProgress(progress);
        }
    }
}
