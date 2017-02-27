package cn.yhq.http.download;

import android.content.Context;


public interface IHttpDownloader {
    void download(Context context, DownloadTask downloadTask);

    void cancel(Context context, DownloadTask downloadTask);
}
