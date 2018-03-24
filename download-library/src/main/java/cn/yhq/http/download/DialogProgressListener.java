package cn.yhq.http.download;

import android.content.Context;
import android.content.DialogInterface;

import java.io.File;

import cn.yhq.dialog.builder.ProgressDialogBuilder;
import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;


/**
 * Created by Administrator on 2016/7/26.
 */
class DialogProgressListener
        implements
        IDownloadProgressListener,
        IDownloaderListener, DialogInterface.OnCancelListener {
    private ProgressDialogBuilder.ProgressHandler mProgressHandler = new ProgressDialogBuilder.ProgressHandler();
    private IDialog mProgressDialog;
    private FileDownloader.Builder builder;
    private Context mContext;
    private String mTaskId;

    public DialogProgressListener(FileDownloader.Builder builder) {
        this.builder = builder;
        this.mContext = builder.getContext();
    }

    @Override
    public void onProgress(int progress) {
        mProgressHandler.setProgress(progress);
    }

    @Override
    public void onException(String taskId, Throwable e) {
        mProgressDialog.dismiss();
    }

    @Override
    public void onSuccess(String taskId, File file) {
        mProgressDialog.dismiss();
    }

    @Override
    public void onStart(String taskId) {
        this.mTaskId = taskId;
        this.mProgressDialog = DialogBuilder.progressDialog(mContext)
                .setMessage("正在下载文件：<br/>" + builder.getLocalFile().getName() + "")
                .setOnCancelListener(this)
                .progressHandler(mProgressHandler).show();
    }

    @Override
    public void onCancel(String taskId) {
        mProgressDialog.dismiss();
    }

    @Override
    public void onPause(String taskId) {

    }

    @Override
    public void onResume(String taskId) {

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        FileDownloader.getDownloader().cancel(this.mTaskId);
    }
}
