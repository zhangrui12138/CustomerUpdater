package com.hongyao.hyupdater.utils;

import android.util.Log;
/*
created by zhangrui for 20240613
*/
public class LogUtils {
    public static boolean debug = true;
    public static final String TAG = "zhangrui-daina";
    public static void messager(String message){
        if(debug) Log.d(TAG,message);
    }
}
