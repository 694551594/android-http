package cn.yhq.http.download;

import java.io.File;

/**
 * Created by Administrator on 2016/7/26.
 */
public class DownloadInterceptor implements IDownloadInterceptor {

    @Override
    public void onStart(String taskId) {
    }

    @Override
    public void onCancel(String taskId) {
    }

    @Override
    public void onPause(String taskId) {
    }

    @Override
    public void onResume(String taskId) {
    }

    @Override
    public void onException(String taskId, Throwable e) {
    }

    @Override
    public void onSuccess(String taskId, File file) {
    }
}
