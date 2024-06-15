package com.hongyao.hyupdater.view.activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.internetmodel.InternetManager;
import com.hongyao.hyupdater.internetmodel.beans.User;
import com.hongyao.hyupdater.ui.HyDialog;
import com.hongyao.hyupdater.ui.HyDialog3Button;
import com.hongyao.hyupdater.utils.LogUtils;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Locale;

import okhttp3.ResponseBody;


/*
created by zhangrui for 20240613
*/

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HyDialog.MyDialogInterface
        ,InternetManager.TimeOutInterface ,HyDialog3Button.HyDialog3ButtonInterface{
    private TextView tv_device_model;
    private TextView tv_device_version;
    private TextView tv_device_serialno;
    private Button btn_check_update;
    private Button btn_back;
    private InternetManager internetManager;
    private String serialno;
    private String otaVersion;
    private String model;
    private String language;
    private ConnectivityManager connectionManager;
    private WaitDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DialogX.init(this);
        language = getSystemLanguage();
        connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        tv_device_model = (TextView)findViewById(R.id.tv_device_model);
        tv_device_version = (TextView)findViewById(R.id.tv_device_version);
        tv_device_serialno = (TextView)findViewById(R.id.tv_device_mid);
        tv_device_serialno.setOnClickListener(this);
        btn_check_update = (Button)findViewById(R.id.btn_check_update);
        btn_check_update.setOnClickListener(this);
        btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        internetManager = new InternetManager(this);
        serialno = Build.SERIAL;
        otaVersion = SystemPropertiesUtils.getProperty("ro.build.display.id", "");
        model = Build.MODEL;
        tv_device_model.setText(model);
        tv_device_version.setText(otaVersion);
        tv_device_serialno.setText(serialno);
    }
    private String getSystemLanguage() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return language + "-" + country;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                HyDialog dialog = new HyDialog(MainActivity.this,
                        MainActivity.this.getString(R.string.back),
                        MainActivity.this.getString(R.string.backMsg),
                        MainActivity.this.getString(R.string.confirmButton),
                        MainActivity.this.getString(R.string.cancelButton),
                        View.GONE,this, Contain.OPERATOR_BACK);
                dialog.setCancelable(false);
                dialog.show();
                break;

            case R.id.btn_check_update:
                NetworkInfo networkinfo = connectionManager.getActiveNetworkInfo();
                if (!(networkinfo != null && networkinfo.isConnected())) {
                    Toast.makeText(MainActivity.this, R.string.netNotAvailable, Toast.LENGTH_SHORT).show();
                    return;
                }
                int currentLevel = getPowerPercent();
                LogUtils.messager("currentLevel="+currentLevel);
                String property = SystemPropertiesUtils.getProperty("hy.battery.power.min", "30");
                double minPowder = Double.parseDouble(property);
                if(currentLevel <= minPowder){
                    Toast.makeText(MainActivity.this, R.string.powerToLow, Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingDialog = WaitDialog.show(MainActivity.this.getString(R.string.checking));
                LogUtils.messager("language:" + language+"///otaVersion"+otaVersion+"///serialno"+serialno+"///model"+model);
                //test start
                //S13_80_P503_EYYB_16_2_V1.03_KC_20231009/51talk20240321002/EYYB-MPW2-P503?language=ja-JP
                /*otaVersion="S13_80_P503_EYYB_16_2_V1.03_KC_20231009";
                serialno = "51talk20240321002";
                model = "EYYB-MPW2-P503";
                language = "ja-JP";*/
                //test end
                internetManager.getRequest(otaVersion,serialno,model,language);
                break;
            case R.id.tv_device_mid:
                doTv_device_midOperation();
                break;
        }
    }

    private String storage = Environment.getExternalStorageDirectory().getPath();
    private String dir = Environment.getDataDirectory().getAbsolutePath() + "/data/com.hongyao.hyupdater";
    private String filePath = dir + "/update.zip";
    private File local_file = new File(storage, "hy_local_update.zip");
    private File local_file1 = new File(dir, "hy_local_update.zip");

    private class LocalUpdateAsyncTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            LogUtils.messager("storage");
            LogUtils.messager("click times:" + clickVersionCount);
            String resulter = "success";
            if (local_file.exists()) {
                try {
                    if (local_file1.exists()) {
                        local_file1.delete();
                    }
                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    fis = new FileInputStream(local_file);
                    fos = new FileOutputStream(local_file1);
                    byte[] buffer = new byte[1024 * 1024 * 5];
                    int len = 0;
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                   }
                }catch (Exception e){
                    resulter = "copyError";
                    LogUtils.messager("e.getMessage()="+e.getMessage());
                }
            } else {
                resulter = "noLocalFile";
            }
            return resulter;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if("success".equals(s)){
                    hyDialog3Button = new HyDialog3Button(MainActivity.this, MainActivity.this.getString(R.string.localUpdate),
                            MainActivity.this.getString(R.string.findLocalZip),MainActivity.this.getString(R.string.updateButton),MainActivity.this
                    ,Contain.OPERATOR_3_NOMAL);
                    hyDialog3Button.show();
            }else if("copyError".equals(s)){
                Toast.makeText(MainActivity.this, R.string.fileDamage, Toast.LENGTH_SHORT).show();
            }else if("noLocalFile".equals(s)){
                Toast.makeText(MainActivity.this, R.string.localZipNotFound, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private User mDownloadUser = null;
    private File mFile = null;
    class NeedUpdateAsync extends AsyncTask<Void,Void,String>{
        private String filePath;
        private String info;
        private File file = null;
        public NeedUpdateAsync(String filePath1,String info1) {
            filePath = filePath1;
            info = info1;
        }

        @Override
        protected String doInBackground(Void... voids) {
            file = new File(filePath);
            mFile = file;
            String fileMD5 = getFileMD5(file);
            return fileMD5;
        }


        @Override
        protected void onPostExecute(String fileMD5) {
            super.onPostExecute(fileMD5);
            LogUtils.messager("md5 from interface:" + md5);
            LogUtils.messager("md5 from fileMD5 file:" + fileMD5);
            if (loadingDialog != null && loadingDialog.isShow()) {
                loadingDialog.dismiss();
            }
            if (file.exists() && md5 != null && md5.length() != 0 && fileMD5 != null && fileMD5.length() != 0 && md5.equals(fileMD5)) {
                String updateButton = MainActivity.this.getString(R.string.updateButton);
                String findAlreadyDownloadZip = MainActivity.this.getString(R.string.findAlreadyDownloadZip);
                HyDialog3Button hyDialog3Button1 = new HyDialog3Button(MainActivity.this,
                        updateButton,
                        findAlreadyDownloadZip,
                        MainActivity.this.getString(R.string.updateButton),MainActivity.this,Contain.OPERATOR_3_ALREADYHAVEPACKAGE);
                hyDialog3Button1.show();
            } else {
                if (file.exists()) {
                    file.delete();
                }
                HyDialog dialog = new HyDialog(MainActivity.this,
                        MainActivity.this.getString(R.string.findNewVersion),
                        info,
                        MainActivity.this.getString(R.string.downloadButton),
                        MainActivity.this.getString(R.string.cancelButton),
                        View.GONE,
                        MainActivity.this,Contain.OPERATOR_DOWNLOAD);
                dialog.show();
            }
        }
    }

    @Override
    public void closeApp() {
        finish();
    }

    @Override
    public void timeOut() {
        if(loadingDialog != null && loadingDialog.isShow()){
            loadingDialog.dismiss();
        }
    }

    private Long lastClickVersionTime = 0L;
    private int clickVersionCount;
    private void doTv_device_midOperation(){
        Long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickVersionTime) <= 800) {
            clickVersionCount++;
        } else {
            clickVersionCount = 0;
        }
        lastClickVersionTime = currentClickTime;
        if (clickVersionCount == 7) {
            localUpdateAsyncTask = new LocalUpdateAsyncTask();
            localUpdateAsyncTask.execute();
        }
    }

    private LocalUpdateAsyncTask localUpdateAsyncTask;
    private final int noNeedUpdate = 0;//error Or the newest version
    private final int needUpdate = 1;//need update
    private final int forceUpdate = 2;//force Update
    @Override
    public void dataSourceRefresh(User user) {
        if(user != null){
           int isneed_update = user.getIs_need_update();
           switch (isneed_update){
               case noNeedUpdate:
                   String msg = user.getMsg();
                   if(msg == null){
                       String errorMsg = "出现异常，请查看服务端控制台！";
                       LogUtils.messager(errorMsg);
                       int downloadError = R.string.downloadError;
                       Toast.makeText(MainActivity.this, downloadError, Toast.LENGTH_SHORT).show();
                   }else {
                       Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                   }
                   break;
               case needUpdate:
                   doNeedUpdate(user);
                   break;
               case forceUpdate:
                   break;
           }
        }else {
            LogUtils.messager("user==null-----------------");
        }
    }

    private String md5;
    private String newVersion;
    private void doNeedUpdate(User userTemp){
        String info = MainActivity.this.getString(R.string.releaseNumber) + userTemp.getVersion_number()
                + "\n" + MainActivity.this.getString(R.string.description) + "\n" + userTemp.getReserve();
        md5 = userTemp.getMd5();
        newVersion = userTemp.getNew_version();
        LogUtils.messager(userTemp.toString());
        mDownloadUser = userTemp;
        NeedUpdateAsync myAsync = new NeedUpdateAsync(filePath,info);
        myAsync.execute();
    }

    private BatteryManager batteryManager;
    private int getPowerPercent(){
        int currentLevel = -1;
        if(batteryManager != null){
            currentLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return currentLevel;
    }

    //安装升级包
    private void installPackage(File file) {
        try {
            RecoverySystem.installPackage(MainActivity.this, file);
        } catch (IOException e) {
            LogUtils.messager("e.getMessage()="+e.getMessage());
            Toast.makeText(MainActivity.this, R.string.dontHaveSystemPermission, Toast.LENGTH_SHORT).show();
        }
    }
    private HyDialog3Button hyDialog3Button;

    @Override
    public void do3_dialog_okButton() {
        clickVersionCount = 0;
        local_file.delete();
        installPackage(local_file1);
        if (hyDialog3Button != null && hyDialog3Button.isShowing()) {
            hyDialog3Button.dismiss();
        }
    }

    @Override
    public void do3_dialog_deleteButton() {
        local_file.delete();
        Toast.makeText(MainActivity.this, R.string.deleteSuccess, Toast.LENGTH_SHORT).show();
        if (hyDialog3Button != null && hyDialog3Button.isShowing()) {
            hyDialog3Button.dismiss();
        }
    }

    @Override
    public void do3_dialog_deleteFile() {
       if(mFile != null){
           mFile.delete();
       }
    }

    @Override
    public void do3_dialog_installPackage() {
        if(mFile != null) {
            installPackage(mFile);
        }
    }

    //获取文件MD5
    public static String getFileMD5(File file) {
        if(!file.exists() || !file.isFile()){
            return null;
        }
        MessageDigest md =null;
        try (FileInputStream fis = new FileInputStream(file)) {
            md =  MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        byte[] md5Bytes = md.digest();

        StringBuilder md5Hex = new StringBuilder();
        for (byte b : md5Bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                md5Hex.append("0");
            }
            md5Hex.append(hex);
        }

        return md5Hex.toString();
    }

    @Override
    public void downloadUpdate() {
        if(mDownloadUser != null) {
            downloadUpdatePackage(mDownloadUser.getDownloadurl());
        }
        mDownloadUser = null;
    }
    //下载
    private HyDialog downloadingDialog;
    private void downloadUpdatePackage(String url) {
        Toast.makeText(MainActivity.this, R.string.startDownloading, Toast.LENGTH_SHORT).show();
        downloadingDialog = new HyDialog(this,
                MainActivity.this.getString(R.string.downloading),
                MainActivity.this.getString(R.string.downloading) + "," + MainActivity.this.getString(R.string.pleasePatience),
                MainActivity.this.getString(R.string.confirmButton),
                MainActivity.this.getString(R.string.cancelButton),
                View.VISIBLE,this,Contain.OPERATOR_DOWNLODINGPROGESS);
        downloadingDialog.show();
        internetManager.downLoadUpdatePackager(url);
    }

    @Override
    public void downloadingProgress() {
        TipDialog.show(MainActivity.this.getString(R.string.downloadComplete), WaitDialog.TYPE.SUCCESS);
        WaitDialog.show(MainActivity.this.getString(R.string.verifying));
        File downloadFile = new File(filePath);
        if (!downloadFile.exists()) {
            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.verifyFiled), Toast.LENGTH_SHORT).show();
            WaitDialog.dismiss();
            return;
        }
        CompareMD5AsyncTask compareMD5AsyncTask = new CompareMD5AsyncTask();
        compareMD5AsyncTask.execute(downloadFile);
    }

    private final String CompareMD5Success = "success";
    private final String CompareMD5Error = "error";
    class CompareMD5AsyncTask extends AsyncTask<File,Void,String>{
        private File downloadFile;

        @Override
        protected String doInBackground(File... files) {
            String result = CompareMD5Error;
            downloadFile = files[0];
            String downloadMd5 = getFileMD5(downloadFile);
            LogUtils.messager("CompareMD5AsyncTask----------md5 from interface:" + md5);
            LogUtils.messager("CompareMD5AsyncTask----------md5 from download file:" + downloadMd5);
            if (downloadMd5.equals(md5)) {
                result = CompareMD5Success;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LogUtils.messager("CompareMD5AsyncTask----------s="+s);
            switch (s){
                case CompareMD5Error:
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.fileDamage), Toast.LENGTH_SHORT).show();
                    WaitDialog.dismiss();
                    break;
                case CompareMD5Success:
                    Toast.makeText(MainActivity.this, R.string.verifyComplete, Toast.LENGTH_SHORT).show();
                    //校验完成将新版本的版本号持久化到本地
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("newVersion", MODE_PRIVATE);
                    sp.edit().putString("newVersion", newVersion).commit();
                    WaitDialog.dismiss();
                    installdialog = new HyDialog(MainActivity.this,
                            MainActivity.this.getString(R.string.downloadComplete),
                            MainActivity.this.getString(R.string.askUpdateNow),
                            MainActivity.this.getString(R.string.confirmButton),
                            MainActivity.this.getString(R.string.cancelButton),
                            View.GONE,MainActivity.this,Contain.OPERATOR_INSTALLPACKAGE);
                    installdialog.show();
                    break;
            }
        }
    }

    private HyDialog installdialog;

    @Override
    public void installPackage() {
        File file = new File(filePath);
        LogUtils.messager("updateFile is exist:" + file.exists());
        installPackage(file);
        if (installdialog !=null && installdialog.isShowing()) {
            installdialog.dismiss();
        }
    }

    @Override
    public void downloadingProgressCancel() {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void updatePackageSuccess(ResponseBody responseBody) {
        downloadZipFileTask = new DownloadZipFileTask();
        downloadZipFileTask.execute(responseBody);
    }
    DownloadZipFileTask downloadZipFileTask;
    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(ResponseBody... urls) {
            saveToDisk(urls[0],filePath);
            return null;
        }
        protected void onProgressUpdate(Pair<Integer, Long>... progress) {
            LogUtils.messager(progress[0].second + " ");
            if (progress[0].first == 100)
                Toast.makeText(getApplicationContext(), "File downloaded successfully", Toast.LENGTH_SHORT).show();

            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                LogUtils.messager("Progress " + currentProgress + "%");
                if(downloadingDialog != null && downloadingDialog.isShowing()){
                    downloadingDialog.setProgressBar(currentProgress);
                }
            }

            if (progress[0].first == -1) {
                Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
            if(downloadingDialog != null && downloadingDialog.isShowing()){
                downloadingDialog.dismiss();
            }
            TipDialog.show(MainActivity.this.getString(R.string.downloadComplete), WaitDialog.TYPE.SUCCESS);
            WaitDialog.show(MainActivity.this.getString(R.string.verifying));
            File downloadFile = new File(filePath);
            if (!downloadFile.exists()) {
                Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.verifyFiled), Toast.LENGTH_SHORT).show();
                WaitDialog.dismiss();
                return;
            }
            CompareMD5AsyncTask compareMD5AsyncTask = new CompareMD5AsyncTask();
            compareMD5AsyncTask.execute(downloadFile);
        }
    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {
            File destinationFile = new File(filePath);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                LogUtils.messager("File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    LogUtils.messager("Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }
                outputStream.flush();
                LogUtils.messager(destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                LogUtils.messager("Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.messager("Failed to save the file!");
            return;
        }
    }

}