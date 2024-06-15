package com.hongyao.hyupdater.utils;

import android.util.Log;

public class LogUtils {
    public static boolean debug = true;
    public static final String TAG = "zhangrui-daina";
    public static void messager(String message){
        if(debug) Log.d(TAG,message);
    }
}
