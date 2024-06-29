package com.hongyao.hyupdater.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.internetmodel.InternetManager;
import com.hongyao.hyupdater.ui.HyDialog;
import com.hongyao.hyupdater.utils.LogUtils;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;
import com.hongyao.hyupdater.view.activity.MainActivity;

import java.util.Locale;
/*
created by zhangrui for 20240613
*/

public class CycleBroadcastReciver extends BroadcastReceiver {
    private AlarmManager am;
    private PendingIntent pi;
    private final String action_checkupdate = "com.hongyao.checkUpdate";
    private final String action_boot = "android.intent.action.BOOT_COMPLETED";
    private InternetManager internetManager;
    private String serialno;
    private String otaVersion;
    private String model;
    private String language;
    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        LogUtils.messager("CycleBroadcastReciver===="+intent.getAction());
        switch (intent.getAction()){
            case action_boot:
                SystemPropertiesUtils.setProperty(Contain.ISQUERY_SHOW, "false");
            break;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            serialno = Build.getSerial();
        }else {
            serialno = Build.SERIAL;
        }
        otaVersion = SystemPropertiesUtils.getProperty("ro.build.display.id", "");
        model = Build.MODEL;
        language = getSystemLanguage(context);
        LogUtils.messager("CycleBroadcastReciver----------------language:" + language+"///otaVersion"+otaVersion+"///serialno"+serialno+"///model"+model);
        if(internetManager != null){
            internetManager.queryState(otaVersion,serialno,model,language,context);
        }
        setAlarmTask();
    }

    private String getSystemLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return language + "-" + country;
    }

    private void init(Context context){
        internetManager = new InternetManager();
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(context, CycleBroadcastReciver.class);
        it.setAction(action_checkupdate);
        pi = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void setAlarmTask() {
        if(am != null && pi != null) {
            //String delayTimeStr = SystemPropertiesUtils.getProperty("persist.sys.hy.updatetime", "10");
            //LogUtils.messager("delayTime:" + delayTimeStr);
            //long delayTime = Long.parseLong(delayTimeStr);
            long triggerAtTime = SystemClock.elapsedRealtime() + 1000 * 60 * /*60 * delayTime*/30;
            cancelAlarmTask();
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    private void cancelAlarmTask() {
        if(am != null && pi != null) {
            am.cancel(pi);
        }
    }
}
