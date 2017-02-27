package cn.yhq.http.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import retrofit2.Response;

/**
 * 一个统一处理http各种回调的handler，负责将回调发送到ui线程里。
 * <p>
 * Created by Yanghuiqiang on 2016/8/11.
 */
final class CallUIHandler<T> extends Handler {
    private IHttpRequestListener mHttpRequestListener;
    private IHttpResponseListener<T> mHttpResponseListener;
    // 弱引用，因为可能会造成内存泄漏
    private WeakReference<Context> mContextRef;

    // 请求开始
    public final static int MSG_REQUEST_START = 1;
    // 请求异常
    public final static int MSG_REQUEST_EXCEPTION = 2;
    // 请求进度
    public final static int MSG_REQUEST_PROGRESS = 3;
    // 响应进度
    public final static int MSG_RESPONSE_PROGRESS = 4;
    // 响应成功
    public final static int MSG_RESPONSE_SUCCESS = 5;
    // 响应失败
    public final static int MSG_RESPONSE_FAILURE = 6;
    // 响应缓存
    public final static int MSG_RESPONSE_CACHE = 7;

    public CallUIHandler(Context context) {
        super(Looper.getMainLooper());
        this.mContextRef = new WeakReference<>(context);
    }

    private static class ProgressInfo {
        long bytesRead;
        long contentLength;
        boolean multipart;
        boolean done;
    }

    private static class RequestInfo {
        ICancelable cancelable;
        int requestCode;
        Throwable throwable;
    }

    private static class ResponseInfo<T> {
        Response<T> response;
        int requestCode;
        Throwable throwable;
        T responseData;
    }

    private void handleResponseMessage(Message msg) {
        ResponseInfo<T> responseInfo = (ResponseInfo<T>) msg.obj;
        int requestCode = responseInfo.requestCode;
        Throwable t = responseInfo.throwable;
        Context context = this.mContextRef.get();
        if (context == null) {
            return;
        }
        switch (msg.what) {
            case MSG_RESPONSE_SUCCESS:
                Response<T> response = responseInfo.response;
                boolean isFromCache = response.raw().cacheResponse() != null;
                if (mHttpResponseListener != null) {
                    mHttpResponseListener.onResponse(context, requestCode, responseInfo.responseData, isFromCache);
                }
                if (mHttpRequestListener != null) {
                    mHttpRequestListener.onComplete(requestCode);
                }
                break;
            case MSG_RESPONSE_FAILURE:
                if (mHttpResponseListener != null) {
                    mHttpResponseListener.onException(context, t);
                }
                if (mHttpRequestListener != null) {
                    mHttpRequestListener.onException(context, requestCode, t);
                    mHttpRequestListener.onComplete(requestCode);
                }
                break;
            case MSG_RESPONSE_CACHE:
                if (mHttpResponseListener != null) {
                    mHttpResponseListener.onResponse(context, requestCode, responseInfo.responseData, true);
                }
                break;
        }
    }

    private void handleRequestMessage(Message msg) {
        RequestInfo requestInfo = (RequestInfo) msg.obj;
        ICancelable cancelable = requestInfo.cancelable;
        int requestCode = requestInfo.requestCode;
        Throwable t = requestInfo.throwable;
        Context context = this.mContextRef.get();
        if (context == null) {
            return;
        }
        switch (msg.what) {
            case MSG_REQUEST_START:
                if (mHttpRequestListener != null) {
                    mHttpRequestListener.onStart(context, cancelable, requestCode);
                }
                break;
            case MSG_REQUEST_EXCEPTION:
                if (mHttpRequestListener != null) {
                    mHttpRequestListener.onException(context, requestCode, t);
                    mHttpRequestListener.onComplete(requestCode);
                }
                break;
        }
    }

    @Deprecated
    private void handleProgressMessage(Message msg) {
        ProgressInfo progressInfo = (ProgressInfo) msg.obj;
        long bytesRead = progressInfo.bytesRead;
        long contentLength = progressInfo.contentLength;
        boolean multipart = progressInfo.multipart;
        boolean done = progressInfo.done;
        switch (msg.what) {
            case MSG_REQUEST_PROGRESS:
                break;
            case MSG_RESPONSE_PROGRESS:
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REQUEST_START:
            case MSG_REQUEST_EXCEPTION:
                handleRequestMessage(msg);
                break;
            case MSG_REQUEST_PROGRESS:
            case MSG_RESPONSE_PROGRESS:
                handleProgressMessage(msg);
                break;
            case MSG_RESPONSE_CACHE:
            case MSG_RESPONSE_SUCCESS:
            case MSG_RESPONSE_FAILURE:
                handleResponseMessage(msg);
                break;
        }
    }

    public void requestStart(ICancelable cancelable, int requestCode) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.cancelable = cancelable;
        requestInfo.requestCode = requestCode;
        this.sendMessage(Message.obtain(this, MSG_REQUEST_START, requestInfo));
    }

    public void requestException(Throwable throwable, int requestCode) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.throwable = throwable;
        requestInfo.requestCode = requestCode;
        this.sendMessage(Message.obtain(this, MSG_REQUEST_EXCEPTION, requestInfo));
    }

    public void responseCache(T responseData, int requestCode) {
        ResponseInfo<T> responseInfo = new ResponseInfo<>();
        responseInfo.requestCode = requestCode;
        responseInfo.responseData = responseData;
        this.sendMessage(Message.obtain(this, MSG_RESPONSE_CACHE, responseInfo));
    }

    public void responseSuccess(Response<T> response, int requestCode) {
        if (!response.isSuccessful()) {
            ErrorMessage msg = new ErrorMessage(response.code(), response.message());
            this.responseException(new HttpRequestException(msg), requestCode);
        } else {
            ResponseInfo<T> responseInfo = new ResponseInfo<>();
            responseInfo.requestCode = requestCode;
            responseInfo.response = response;
            responseInfo.responseData = response.body();
            this.sendMessage(Message.obtain(this, MSG_RESPONSE_SUCCESS, responseInfo));
        }
    }

    public void responseException(Throwable throwable, int requestCode) {
        ResponseInfo<T> responseInfo = new ResponseInfo<>();
        responseInfo.requestCode = requestCode;
        responseInfo.throwable = throwable;
        this.sendMessage(Message.obtain(this, MSG_RESPONSE_FAILURE, responseInfo));
    }

    @Deprecated
    public void progress(int msg, boolean multipart, long bytesRead, long contentLength,
                         boolean done) {
        ProgressInfo progressInfo = new ProgressInfo();
        progressInfo.bytesRead = bytesRead;
        progressInfo.contentLength = contentLength;
        progressInfo.multipart = multipart;
        progressInfo.done = done;
        this.sendMessage(Message.obtain(this, msg, progressInfo));
    }

    @Deprecated
    public void requestProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
        progress(MSG_REQUEST_PROGRESS, multipart, bytesRead, contentLength, done);
    }

    @Deprecated
    public void responseProgress(boolean multipart, long bytesRead, long contentLength,
                                 boolean done) {
        progress(MSG_RESPONSE_PROGRESS, multipart, bytesRead, contentLength, done);
    }

    public void setHttpRequestListener(IHttpRequestListener httpRequestListener) {
        this.mHttpRequestListener = httpRequestListener;
    }

    public void setHttpResponseListener(IHttpResponseListener<T> httpResponseListener) {
        this.mHttpResponseListener = httpResponseListener;
    }
}
