package cn.yhq.http.core;

import android.content.Context;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


class DefaultHttpExceptionListener implements IHttpExceptionHandler {

    @Override
    public void onException(Context context, Throwable t) {
        // 服务器连接失败，请稍后重试
        // ErrorMessage errorMsg = ErrorMessage.ERR_HTTP_STATUS_CODE;
        ErrorMessage errorMsg = null;
        if (t instanceof ConnectException || t instanceof UnknownHostException) {
            // 网络连接错误
            if (!Util.isNetworkConnected(context)) {
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

}
