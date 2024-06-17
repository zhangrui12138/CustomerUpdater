package com.hongyao.hyupdater.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RecoverySystem;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hongyao.hyupdater.Contain;
import com.hongyao.hyupdater.R;
import com.hongyao.hyupdater.internetmodel.InternetManager;
import com.hongyao.hyupdater.internetmodel.beans.User;
import com.hongyao.hyupdater.utils.LogUtils;
import com.hongyao.hyupdater.utils.SystemPropertiesUtils;
import com.hongyao.hyupdater.view.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.ResponseBody;
/*
created by zhangrui for 20240613
*/

public class UpdatePackageInstallService extends Service implements InternetManager.TimeOutInterface {
    private String md5;
    private InternetManager internetManager;
    private User responseBean;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.responseBean = (User) intent.getSerializableExtra("queryStateUser");
        LogUtils.messager("UpdatePackageInstallService----------responseBean="+responseBean.toString());
        md5 = responseBean.getMd5();
        internetManager = new InternetManager(this);
        this.responseBean = responseBean;
        MyAsync myAsync = new MyAsync(filePath,responseBean);
        myAsync.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    //安装升级包
    private void installPackage(File file) {
        try {
            RecoverySystem.installPackage(getApplicationContext(), file);
        } catch (IOException e) {
            SystemPropertiesUtils.setProperty(Contain.INSTALL_NO_UI,"false");
            LogUtils.messager("UpdatePackageInstallService----------e.getMessage()="+e.getMessage());
            Toast.makeText(getApplicationContext(), R.string.dontHaveSystemPermission, Toast.LENGTH_SHORT).show();
        }
        stopSelf();
    }


    class MyAsync extends AsyncTask<Void,Void,String>{
        private String filePath;
        private File file = null;
        private User responseBean;
        public MyAsync(String filePath1,User responseBean1) {
            filePath = filePath1;
            responseBean = responseBean1;
        }

        @Override
        protected String doInBackground(Void... voids) {
            file = new File(filePath);
            String fileMD5 = getFileMD5(file);
            return fileMD5;
        }

        @Override
        protected void onPostExecute(String fileMD5) {
            super.onPostExecute(fileMD5);
            LogUtils.messager("UpdatePackageInstallService----------md5 from interface:" + md5);
            LogUtils.messager("UpdatePackageInstallService----------md5 from fileMD5 file:" + fileMD5);
            if (file.exists() && md5 != null && md5.length() != 0 && fileMD5 != null && fileMD5.length() != 0 && md5.equals(fileMD5)) {
                installPackage(file);
            } else {
                if (file.exists()) {
                    file.delete();
                }
                downloadUpdate(responseBean.getDownloadurl());
            }
        }
    }
    private void downloadUpdate(String url) {
        internetManager.downLoadUpdatePackager(url);
    }

    @Override
    public void timeOut() {

    }

    @Override
    public void dataSourceRefresh(User user) {

    }

    @Override
    public void updatePackageSuccess(ResponseBody responseBody) {
        downloadZipFileTask = new DownloadZipFileTask();
        downloadZipFileTask.execute(responseBody);
    }


    private String dir = Environment.getDataDirectory().getAbsolutePath() + "/data/com.hongyao.hyupdater";
    private String filePath = dir + "/update.zip";
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
            LogUtils.messager("UpdatePackageInstallService----------"+progress[0].second + " ");
            if (progress[0].first == 100)
                Toast.makeText(getApplicationContext(), "File downloaded successfully", Toast.LENGTH_SHORT).show();

            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                LogUtils.messager("UpdatePackageInstallService----------Progress " + currentProgress + "%");
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
            File file = new File(filePath);
            LogUtils.messager("UpdatePackageInstallService----------updateFile is exist:" + file.exists());
            installPackage(file);
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
                LogUtils.messager("UpdatePackageInstallService----------File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    LogUtils.messager("Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }
                outputStream.flush();
                LogUtils.messager("UpdatePackageInstallService----------"+destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                SystemPropertiesUtils.setProperty(Contain.INSTALL_NO_UI,"false");
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                LogUtils.messager("UpdatePackageInstallService----------Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            SystemPropertiesUtils.setProperty(Contain.INSTALL_NO_UI,"false");
            LogUtils.messager("UpdatePackageInstallService----------Failed to save the file!");
            return;
        }
    }
}
