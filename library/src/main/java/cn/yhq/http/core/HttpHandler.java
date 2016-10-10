package cn.yhq.http.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 一个统一处理http各种回调的handler，负责将回调发送到ui线程里。
 *
 * Created by Yanghuiqiang on 2016/8/11.
 */
public final class HttpHandler<T> extends Handler {
  private IHttpRequestListener<T> mHttpRequestListener;
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

  public HttpHandler(Context context, IHttpRequestListener<T> httpRequestListener,
      IHttpResponseListener<T> httpResponseListener) {
    super(Looper.getMainLooper());
    this.mContextRef = new WeakReference<>(context);
    this.mHttpRequestListener = httpRequestListener;
    this.mHttpResponseListener = httpResponseListener;
  }

  private static class ProgressInfo {
    long bytesRead;
    long contentLength;
    boolean multipart;
    boolean done;
  }

  private static class RequestInfo<T> {
    Call<T> call;
    int requestCode;
    Throwable throwable;
  }

  private static class ResponseInfo<T> {
    Response<T> response;
    int requestCode;
    Throwable throwable;
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
          mHttpResponseListener.onResponse(context, requestCode, response.body(), isFromCache);
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
          mHttpRequestListener.onException(requestCode, t);
          mHttpRequestListener.onComplete(requestCode);
        }
        break;
    }
  }

  private void handleRequestMessage(Message msg) {
    RequestInfo<T> requestInfo = (RequestInfo<T>) msg.obj;
    Call<T> call = requestInfo.call;
    int requestCode = requestInfo.requestCode;
    Throwable t = requestInfo.throwable;
    switch (msg.what) {
      case MSG_REQUEST_START:
        if (mHttpRequestListener != null) {
          mHttpRequestListener.onStart(call, requestCode);
        }
        break;
      case MSG_REQUEST_EXCEPTION:
        if (mHttpRequestListener != null) {
          mHttpRequestListener.onException(requestCode, t);
          mHttpRequestListener.onComplete(requestCode);
        }
        break;
    }
  }

  private void handleProgressMessage(Message msg) {
    ProgressInfo progressInfo = (ProgressInfo) msg.obj;
    long bytesRead = progressInfo.bytesRead;
    long contentLength = progressInfo.contentLength;
    boolean multipart = progressInfo.multipart;
    boolean done = progressInfo.done;
    switch (msg.what) {
      case MSG_REQUEST_PROGRESS:
        if (mHttpRequestListener != null) {
          mHttpRequestListener.onRequestProgress(multipart, bytesRead, contentLength, done);
        }
        break;
      case MSG_RESPONSE_PROGRESS:
        if (mHttpRequestListener != null) {
          mHttpRequestListener.onResponseProgress(multipart, bytesRead, contentLength, done);
        }
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
      case MSG_RESPONSE_SUCCESS:
      case MSG_RESPONSE_FAILURE:
        handleResponseMessage(msg);
        break;
    }
  }

  public void requestStart(Call<T> call, int requestCode) {
    RequestInfo<T> requestInfo = new RequestInfo<T>();
    requestInfo.call = call;
    requestInfo.requestCode = requestCode;
    this.sendMessage(Message.obtain(this, MSG_REQUEST_START, requestInfo));
  }

  public void requestException(Throwable throwable, int requestCode) {
    RequestInfo<T> requestInfo = new RequestInfo<T>();
    requestInfo.throwable = throwable;
    requestInfo.requestCode = requestCode;
    this.sendMessage(Message.obtain(this, MSG_REQUEST_EXCEPTION, requestInfo));
  }

  public void responseSuccess(Response<T> response, int requestCode) {
    if (!response.isSuccessful()) {
      ErrorMessage msg = new ErrorMessage(response.code(), response.message());
      this.responseException(new HttpRequestException(msg), requestCode);
    } else {
      ResponseInfo<T> responseInfo = new ResponseInfo<T>();
      responseInfo.requestCode = requestCode;
      responseInfo.response = response;
      this.sendMessage(Message.obtain(this, MSG_RESPONSE_SUCCESS, responseInfo));
    }
  }

  public void responseException(Throwable throwable, int requestCode) {
    ResponseInfo<T> responseInfo = new ResponseInfo<T>();
    responseInfo.requestCode = requestCode;
    responseInfo.throwable = throwable;
    this.sendMessage(Message.obtain(this, MSG_RESPONSE_FAILURE, responseInfo));
  }

  public void progress(int msg, boolean multipart, long bytesRead, long contentLength,
      boolean done) {
    ProgressInfo progressInfo = new ProgressInfo();
    progressInfo.bytesRead = bytesRead;
    progressInfo.contentLength = contentLength;
    progressInfo.multipart = multipart;
    progressInfo.done = done;
    this.sendMessage(Message.obtain(this, msg, progressInfo));
  }

  public void requestProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
    progress(MSG_REQUEST_PROGRESS, multipart, bytesRead, contentLength, done);
  }

  public void responseProgress(boolean multipart, long bytesRead, long contentLength,
      boolean done) {
    progress(MSG_RESPONSE_PROGRESS, multipart, bytesRead, contentLength, done);
  }
}
