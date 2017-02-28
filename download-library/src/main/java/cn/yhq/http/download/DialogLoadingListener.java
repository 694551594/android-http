package cn.yhq.http.download;

import android.content.Context;
import android.content.DialogInterface;

import java.io.File;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;

/**
 * Created by Yanghuiqiang on 2017/2/28.
 */

class DialogLoadingListener implements
        IDownloadProgressListener,
        IDownloaderListener, DialogInterface.OnCancelListener {
    private IDialog mLoadingDialog;
    private FileDownloader.Builder builder;
    private Context mContext;
    private String mTaskId;

    public DialogLoadingListener(FileDownloader.Builder builder) {
        this.builder = builder;
        this.mContext = builder.getContext();
    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onStart(String taskId) {
        this.mTaskId = taskId;
        this.mLoadingDialog = DialogBuilder.loadingDialog0(mContext)
                .setMessage("正在下载文件：<br/>" + builder.getLocalFile().getName() + "")
                .setOnCancelListener(this).show();
    }

    @Override
    public void onCancel(String taskId) {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onPause(String taskId) {

    }

    @Override
    public void onResume(String taskId) {

    }

    @Override
    public void onException(String taskId, Throwable e) {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onSuccess(String taskId, File file) {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        FileDownloader.getDownloader().cancel(this.mTaskId);
    }
}
