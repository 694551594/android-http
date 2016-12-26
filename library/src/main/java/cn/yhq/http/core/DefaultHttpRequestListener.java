package cn.yhq.http.core;

import android.content.Context;
import android.content.DialogInterface;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;

/**
 * 请求监听，实现请求过程中显示loading对话框
 *
 * @author Yanghuiqiang 2015-10-9
 */
class DefaultHttpRequestListener
        extends
        HttpRequestListener implements
        DialogInterface.OnCancelListener {
    private ICancelable mCancelable;
    private IDialog mLoadingDialog;

    public DefaultHttpRequestListener() {
    }

    @Override
    public void onStart(Context context, ICancelable cancelable, int requestCode) {
        this.mCancelable = cancelable;
        mLoadingDialog = DialogBuilder.loadingDialog0(context).setOnCancelListener(this).show();
    }

    @Override
    public void onComplete(int requestCode) {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.mCancelable.cancel();
    }
}
