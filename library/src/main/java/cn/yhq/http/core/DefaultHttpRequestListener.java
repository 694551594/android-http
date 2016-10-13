package cn.yhq.http.core;

import android.content.Context;
import android.content.DialogInterface;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;

/**
 * 请求监听，实现请求过程中显示loading对话框
 *
 * @param <T>
 * @author Yanghuiqiang 2015-10-9
 */
public class DefaultHttpRequestListener<T>
        extends
        HttpRequestListener<T> implements
        DialogInterface.OnCancelListener {
    private HttpRequester<T> httpRequester;
    private IDialog mLoadingDialog;

    public DefaultHttpRequestListener() {
    }

    @Override
    public void onStart(Context context, HttpRequester<T> httpRequester, int requestCode) {
        this.httpRequester = httpRequester;
        mLoadingDialog = DialogBuilder.loadingDialog(context).setOnCancelListener(this).show();
    }

    @Override
    public void onComplete(int requestCode) {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.httpRequester.cancel();
    }
}
