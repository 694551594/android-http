package cn.yhq.http.download;


public interface IDownloaderListener extends IDownloadResponseListener {

    void onStart(String taskId);

    void onCancel(String taskId);

    void onPause(String taskId);

    void onResume(String taskId);
}
