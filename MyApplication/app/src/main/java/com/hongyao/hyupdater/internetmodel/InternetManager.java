package com.hongyao.hyupdater.internetmodel;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.internetmodel.beans.User;
import com.hongyao.hyupdater.services.UpdatePackageInstallService;
import com.hongyao.hyupdater.ui.HyDialog;
import com.hongyao.hyupdater.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/*
created by zhangrui for 20240613
*/

public class InternetManager {
    private Retrofit retrofit = null;
    private IUserService iUserService = null;
    private User user = null;
    private static OkHttpClient client = null;
    public TimeOutInterface timeOutInterface;
    private Calendar mCalendar = Calendar.getInstance();
    public InternetManager(TimeOutInterface timeOutInterface) {
        this.timeOutInterface = timeOutInterface;
        if(client == null){
            client = new OkHttpClient.Builder().
                    connectTimeout(30, TimeUnit.SECONDS).
                    readTimeout(30, TimeUnit.SECONDS).
                    writeTimeout(30, TimeUnit.SECONDS).build();
        }
        if(retrofit == null){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(Contain.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        if(iUserService == null){
            iUserService = retrofit.create(IUserService.class);
        }
    }

    public InternetManager() {
        if(client == null){
            client = new OkHttpClient.Builder().
                    connectTimeout(30, TimeUnit.SECONDS).
                    readTimeout(30, TimeUnit.SECONDS).
                    writeTimeout(30, TimeUnit.SECONDS).build();
        }
        if(retrofit == null){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(Contain.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        if(iUserService == null){
            iUserService = retrofit.create(IUserService.class);
        }
    }


    public void getRequest(String version,String serialno,String model,String language) {
        Call<User> call = iUserService.getUser(version,serialno,model,language);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                LogUtils.messager("request = " + call.request().toString());
                LogUtils.messager("response = " + response);
                user = response.body();
                LogUtils.messager("user.toString() = " + user.toString());
                timeOutInterface.dataSourceRefresh(user);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                LogUtils.messager("t.getMessage() = " + t.getMessage());
                timeOutInterface.timeOut();
            }
        });
    }
    public interface TimeOutInterface{
        void timeOut();
        void dataSourceRefresh(User user);
        void updatePackageSuccess(ResponseBody responseBody);
    }
    public void downLoadUpdatePackager(String url){
        Call<ResponseBody> call = iUserService.downloadFileWithc(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    timeOutInterface.updatePackageSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private User queryStateUser = null;
    private HyDialog dialog;
    public void queryState(String version, String serialno, String model, String language,Context context) {
        Call<User> call = iUserService.getUser(version,serialno,model,language);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                queryStateUser = response.body();
                int is_need_update = queryStateUser.getIs_need_update();
                String isShow = SystemPropertiesUtils.getProperty(Contain.ISQUERY_SHOW,"false");
                String topPackageName = getTopPkgName(context);
                LogUtils.messager("queryState-------------user.toString() = "
                        + queryStateUser.toString()
                        +"///is_need_update="+is_need_update
                        +"///isShow="+isShow
                        +"///topPackageName="+topPackageName
                );
                if(is_need_update == 1){//nomal update 1
                    if(!("true".equals(isShow)) && !("com.hongyao.hyupdater".equals(topPackageName))) {
                        SystemPropertiesUtils.setProperty(Contain.ISQUERY_SHOW, "true");
                        if (dialog == null) {
                            dialog = new HyDialog(context, context.getString(R.string.findNewVersion),
                                    context.getString(R.string.askGoUpdate),
                                    context.getString(R.string.confirmButton),
                                    context.getString(R.string.cancelButton),
                                    View.GONE,
                                    null, Contain.OPERATOR_QUERY_STATE);
                        }
                        if (!dialog.isShowing()) {
                            LogUtils.messager("sdk:" + Build.VERSION.SDK_INT);
                            if (Build.VERSION.SDK_INT >= 26) {
                                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                            } else if (Build.VERSION.SDK_INT >= 23) {
                                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                            } else {
                                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            }
                            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                            dialog.show();
                        }
                    }
                }else if(is_need_update == 2){//force update 2
                    int deviceHour = -1;
                    int requestHour = -2;
                    if(queryStateUser != null){
                        requestHour = queryStateUser.getForce_update_time();
                    }
                    if(mCalendar != null){
                        deviceHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                        LogUtils.messager("deviceHour="+deviceHour);
                    }
                    if(requestHour == deviceHour) {
                        Intent tempIntent = new Intent(context, UpdatePackageInstallService.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("queryStateUser",queryStateUser);
                        tempIntent.putExtras(bundle);
                        context.startService(tempIntent);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                LogUtils.messager("t.getMessage() = " + t.getMessage());
            }
        });
    }

    private String getTopPkgName(Context context) {
        if (context == null) {
            return null;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfoList = null;
        if (manager != null) {
            taskInfoList = manager.getRunningTasks(1);
        }
        if (taskInfoList == null || taskInfoList.size() == 0) {
            return null;
        }
        // 返回第一个，可以看出上面那个是栈，而这个是列表的存储数据结构
        return taskInfoList.get(0).topActivity.getPackageName();
    }

}
