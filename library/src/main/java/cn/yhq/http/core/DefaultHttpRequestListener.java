package cn.yhq.http.core;

import android.content.Context;
import android.content.DialogInterface;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;
import retrofit2.Call;

/**
 * 请求监听，实现请求过程中显示loading对话框
 * 
 * @author Yanghuiqiang 2015-10-9
 * 
 * @param <T>
 */
public class DefaultHttpRequestListener<T>
    implements
      IHttpRequestListener<T>,
      DialogInterface.OnCancelListener {
  private Call<T> mCall;
  private IDialog mLoadingDialog;

  public DefaultHttpRequestListener(Context context) {
    if (context == null) {
      return;
    }
    mLoadingDialog = DialogBuilder.loadingDialog(context).setOnCancelListener(this).create();
  }

  @Override
  public void onStart(Call<T> call, int requestCode) {
    this.mCall = call;
    mLoadingDialog.show();
  }

  @Override
  public void onException(int requestCode, Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onComplete(int requestCode) {
    mLoadingDialog.dismiss();
  }

  @Override
  public void onRequestProgress(boolean multipart, long bytesRead, long contentLength,
      boolean done) {}

  @Override
  public void onResponseProgress(boolean multipart, long bytesRead, long contentLength,
      boolean done) {}

  @Override
  public void onCancel(DialogInterface dialog) {
    this.mCall.cancel();
  }
}
