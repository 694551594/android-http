package cn.yhq.http.core;

import android.content.Context;

import cn.yhq.http.core.interceptor.AuthTokenInterceptor;
import cn.yhq.http.core.interceptor.AuthenticatorInterceptor;
import cn.yhq.http.core.interceptor.HttpRequestCacheInterceptor;
import cn.yhq.http.core.interceptor.HttpRequestProgressInterceptor;
import cn.yhq.http.core.interceptor.HttpResponseProgressInterceptor;
import cn.yhq.http.core.interceptor.ProgressListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

final class XCall<T> implements ICall<T> {
    // 拦截器
    private final static HttpRequestCacheInterceptor mHttpRequestCacheInterceptor = new HttpRequestCacheInterceptor();
    private final static HttpResponseProgressInterceptor mHttpResponseProgressInterceptor = new HttpResponseProgressInterceptor();
    private final static HttpRequestProgressInterceptor mHttpRequestProgressInterceptor = new HttpRequestProgressInterceptor();
    private final static AuthenticatorInterceptor mAuthenticatorInterceptor = new AuthenticatorInterceptor();
    private final static AuthTokenInterceptor mAuthTokenInterceptor = new AuthTokenInterceptor();

    private static IHttpRequestListener mDefaultHttpRequestListener;
    private static IHttpExceptionHandler mDefaultHttpExceptionHandler;

    // 缓存有效时间
    public final static int CACHE_MAX_STALE = 7 * 24 * 3600;
    private final static CacheStrategy CACHE_STRATEGY = CacheStrategy.ONLY_NETWORK;

    private IHttpRequestListener mHttpRequestListener;
    private IHttpResponseListener<T> mHttpResponseListener;
    private IHttpExceptionHandler mHttpExceptionHandler;
    private Call<T> mCall;
    private Response<T> mResponse;
    private CallUIHandler mCallUIHandler;
    private int mRequestCode;
    private boolean isAsync = true;

    private HttpRequestListenerProxy mHttpRequestListenerProxy = new HttpRequestListenerProxy();
    private HttpResponseListenerProxy mHttpResponseListenerProxy = new HttpResponseListenerProxy();

    static {
        setCacheStrategy(CACHE_STRATEGY, CACHE_MAX_STALE);
        setDefaultHttpExceptionHandler(new DefaultHttpExceptionListener());
        setDefaultHttpRequestListener(new DefaultHttpRequestListener());
    }

    // 回调处理
    private Callback<T> mUICallback = new Callback<T>() {

        @Override
        public void onResponse(Call<T> call, retrofit2.Response<T> response) {
            mCallUIHandler.responseSuccess(response, mRequestCode);
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            mCallUIHandler.responseException(t, mRequestCode);
        }
    };

    class HttpResponseListenerProxy extends HttpResponseListener<T> {

        @Override
        public void onResponse(Context context, int requestCode, T response, boolean isFromCache) {
            super.onResponse(context, requestCode, response, isFromCache);
            if (mHttpResponseListener != null) {
                mHttpResponseListener.onResponse(context, requestCode, response, isFromCache);
            }
        }

        @Override
        public void onException(Context context, Throwable t) {
            super.onException(context, t);
            if (mHttpExceptionHandler != null) {
                mHttpExceptionHandler.onException(context, t);
            }
            if (mHttpResponseListener != null) {
                mHttpResponseListener.onException(context, t);
            }
        }
    }

    class HttpRequestListenerProxy extends HttpRequestListener {

        HttpRequestListenerProxy() {
        }

        @Override
        public void onStart(Context context, ICancelable cancelable, int requestCode) {
            if (mHttpRequestListener != null) {
                mHttpRequestListener.onStart(context, cancelable, requestCode);
            }
        }

        @Override
        public void onException(Context context, int requestCode, Throwable t) {
            if (mHttpExceptionHandler != null) {
                mHttpExceptionHandler.onException(context, t);
            }
            if (mHttpRequestListener != null) {
                mHttpRequestListener.onException(context, requestCode, t);
            }
        }

        @Override
        public void onComplete(int requestCode) {
            if (mHttpRequestListener != null) {
                mHttpRequestListener.onComplete(requestCode);
            }
        }

        @Override
        public void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
            if (mHttpRequestListener != null) {
                mHttpRequestListener.onRequestProgress(multipart, bytesRead, contentLength, done);
            }
        }

        @Override
        public void onResponseProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
            if (mHttpRequestListener != null) {
                mHttpRequestListener.onResponseProgress(multipart, bytesRead, contentLength, done);
            }
        }
    }

    private ProgressListener mResponseProgressListener = new ProgressListener() {

        @Override
        public void update(Request request, boolean multipart, long bytesRead, long contentLength, boolean done) {
            mCallUIHandler.responseProgress(multipart, bytesRead, contentLength, done);
        }
    };

    private ProgressListener mRequestProgressListener = new ProgressListener() {

        @Override
        public void update(Request request, boolean multipart, long bytesRead, long contentLength, boolean done) {
            mCallUIHandler.requestProgress(multipart, bytesRead, contentLength, done);
        }
    };

    public static void setAuthTokenHandler(AuthTokenHandler handler) {
        mAuthenticatorInterceptor.setAuthTokenHandler(handler);
        mAuthTokenInterceptor.setAuthTokenHandler(handler);
    }

    public static void setCacheStrategy(CacheStrategy cacheStrategy, int cacheStale) {
        mHttpRequestCacheInterceptor.setCacheStrategy(cacheStrategy, cacheStale);
    }

    public static void setDefaultHttpExceptionHandler(IHttpExceptionHandler httpExceptionHandler) {
        mDefaultHttpExceptionHandler = httpExceptionHandler;
    }

    public static void setDefaultHttpRequestListener(IHttpRequestListener httpRequestListener) {
        mDefaultHttpRequestListener = httpRequestListener;
    }

    public static void init(OkHttpClient.Builder builder) {
        builder.addInterceptor(mHttpRequestCacheInterceptor)
                .addInterceptor(mHttpRequestProgressInterceptor)
                .addInterceptor(mHttpResponseProgressInterceptor)
                .authenticator(mAuthenticatorInterceptor)
                .addNetworkInterceptor(mAuthTokenInterceptor);
    }

    public XCall(Call<T> call) {
        this.mCall = call;
        this.mHttpExceptionHandler = mDefaultHttpExceptionHandler;
        this.mHttpRequestListener = mDefaultHttpRequestListener;
    }

    @Override
    public ICall<T> requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    @Override
    public ICallResponse<T> execute(Context context, IHttpResponseListener<T> listener) {
        return execute(context, mHttpRequestListener, listener);
    }

    @Override
    public ICallResponse<T> execute(Context context, IHttpRequestListener requestListener, IHttpResponseListener<T> responseListener) {
        // 拦截器
        mHttpResponseProgressInterceptor.setProgressListener(mResponseProgressListener);
        mHttpRequestProgressInterceptor.setProgressListener(mRequestProgressListener);
        // 监听器
        this.mCallUIHandler = new CallUIHandler(context);
        this.mHttpRequestListener = requestListener;
        this.mHttpResponseListener = responseListener;
        this.mCallUIHandler.setHttpRequestListener(mHttpRequestListenerProxy);
        this.mCallUIHandler.setHttpResponseListener(mHttpResponseListenerProxy);

        return handleRequest();
    }

    @Override
    public ICall<T> async(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    @Override
    public ICallExecutor<T> exceptionHandler(IHttpExceptionHandler handler) {
        this.mHttpExceptionHandler = handler;
        return this;
    }

    @Override
    public void cancel() {
        mCall.cancel();
    }

    private ICallResponse<T> handleRequest() {
        ICallResponse<T> callResponse = new ICallResponse<T>() {
            @Override
            public T getResponseBody() {
                if (mResponse == null) {
                    return null;
                }
                return mResponse.body();
            }

            @Override
            public Response getResponse() {
                return mResponse;
            }

        };
        try {
            mCallUIHandler.requestStart(this, mRequestCode);
            // 真正开始请求的地方
            if (isAsync) {
                // 异步执行
                mCall.enqueue(mUICallback);
            } else {
                // 同步执行
                mResponse = mCall.execute();
                mUICallback.onResponse(mCall, mResponse);
            }
        } catch (Throwable t) {
            mCallUIHandler.requestException(t, mRequestCode);
        }
        return callResponse;
    }

}
