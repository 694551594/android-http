package cn.yhq.http.download;

import java.io.File;


public interface IDownloadResponseListener {
    void onException(String taskId, Throwable e);

    void onSuccess(String taskId, File file);
}
