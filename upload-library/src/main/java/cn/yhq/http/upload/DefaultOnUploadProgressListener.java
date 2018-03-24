package cn.yhq.http.upload;

import android.content.Context;

import cn.yhq.dialog.builder.ProgressDialogBuilder;
import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class DefaultOnUploadProgressListener implements OnUploadProgressListener {
    private IDialog dialog;
    private ProgressDialogBuilder.ProgressHandler progressHandler = new ProgressDialogBuilder.ProgressHandler();

    public DefaultOnUploadProgressListener(Context context) {
        dialog = DialogBuilder.progressDialog(context)
                .setMessage("文件正在上传，请稍后...")
                .progressHandler(progressHandler).create();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onProgress(int progress) {
        progressHandler.setProgress(progress);
        if (progress == 0) {
            dialog.show();
        }
        if (progress == 100) {
            dialog.dismiss();
        }
    }

}
