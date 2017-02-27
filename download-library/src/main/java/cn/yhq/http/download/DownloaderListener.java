package cn.yhq.http.download;

import java.io.File;

public class DownloaderListener implements IDownloaderListener {

    @Override
    public void onException(String taskId, Throwable e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSuccess(String taskId, File file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStart(String taskId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCancel(String taskId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause(String taskId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume(String taskId) {
        // TODO Auto-generated method stub

    }

}
