package cn.yhq.http.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


class HttpResponseExceptionListener<T> implements IHttpResponseListener<T> {
  public final static String TAG = "response";

  @Override
  public void onResponse(Context context, int requestCode, T response, boolean isFromCache) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onException(Context context, Throwable t) {
    // 服务器连接失败，请稍后重试
    // ErrorMessage errorMsg = ErrorMessage.ERR_HTTP_STATUS_CODE;
    ErrorMessage errorMsg = null;
    if (t instanceof ConnectException || t instanceof UnknownHostException) {
      // 网络连接错误
      if (!isNetworkConnected(context)) {
        errorMsg = ErrorMessage.ERR_HTTP_NOCONNECTION;
      } else {
        errorMsg = ErrorMessage.ERR_HTTP_SERVICECONNECTION;
      }
    } else if (t instanceof SocketTimeoutException || t instanceof ConnectTimeoutException) {
      errorMsg = ErrorMessage.ERR_HTTP_REFUSEED;
    } else if (t instanceof IOException && "Canceled".equals(t.getLocalizedMessage())) {
      errorMsg = null;
    } else if (t instanceof SocketException) {
      errorMsg = null;
    } else {
      errorMsg = ErrorMessage.ERR_HTTP_STATUS_CODE;
    }
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg.msg, Toast.LENGTH_LONG).show();
    }
  }

  public static boolean isNetworkConnected(Context context) {
    NetworkInfo ni = getActiveNetwork(context);
    return ni != null && ni.isConnectedOrConnecting();
  }

  public static NetworkInfo getActiveNetwork(Context context) {
    if (context == null) return null;
    ConnectivityManager mConnMgr =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (mConnMgr == null) return null;
    NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo();
    return aActiveInfo;
  }
}
