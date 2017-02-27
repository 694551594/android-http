package cn.yhq.http.core;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * http请求，使用了okhttp3以及retrofit2
 *
 * @param <T>
 * @author Yanghuiqiang 2015-10-9
 */
public final class HttpRequester<T> {
    private final static Map<Class<?>, Object> apis = new HashMap<>();
    private static OkHttpClient mOkHttpClient;
    private Context mContext;
    private ICall<T> mCall;
    private ICallResponse<T> mCallResponse;
    private IHttpRequestListener mHttpRequestListener;
    private IHttpResponseListener<T> mHttpResponseListener;

    public static class Builder<T> implements ICallBuilder<Builder<T>> {
        private IHttpRequestListener httpRequestListener;
        private IHttpResponseListener<T> httpResponseListener;
        private IHttpResponseCommonListener httpResponseCommonListener;

        private Context context;
        private int requestCode;
        private CacheStrategy cacheStrategy;
        private boolean async = true;
        private IHttpExceptionHandler httpExceptionHandler;

        private ICall<T> xCall;

        private IHttpResponseListener httpResponseListenerProxy = new HttpResponseListener<T>() {

            @Override
            public void onResponse(Context context, int requestCode, T response, boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
                if (httpResponseListener != null) {
                    httpResponseListener.onResponse(context, requestCode, response, isFromCache);
                }
                if (httpResponseCommonListener != null) {
                    httpResponseCommonListener.onResponse(context, requestCode, response, isFromCache);
                }
            }

            @Override
            public void onException(Context context, Throwable t) {
                super.onException(context, t);
                if (httpResponseListener != null) {
                    httpResponseListener.onException(context, t);
                }
                if (httpResponseCommonListener != null) {
                    httpResponseCommonListener.onException(context, t);
                }
            }
        };

        public Builder(final Context context) {
            this.context = context;
            this.httpRequestListener = new DefaultHttpRequestListener();
        }

        public HttpRequester<T> build() {
            if (this.xCall == null) {
                throw new NullPointerException("没有call对象？");
            }
            xCall.requestCode(requestCode);
            xCall.async(async);
            xCall.exceptionHandler(httpExceptionHandler);
            xCall.cacheStrategy(cacheStrategy);
            HttpRequester<T> httpRequester = new HttpRequester<>(this);
            return httpRequester;
        }

        public ICallResponse<T> request() {
            HttpRequester<T> httpRequester = build();
            return httpRequester.request();
        }

        boolean isAsync() {
            return async;
        }

        /**
         * 设置请求是异步的还是同步的
         *
         * @param async
         * @return
         */
        public Builder<T> setAsync(boolean async) {
            this.async = async;
            return this;
        }

        public Builder<T> async() {
            this.setAsync(true);
            return this;
        }

        public Builder<T> sync() {
            this.setAsync(false);
            return this;
        }

        @Override
        public Builder<T> async(boolean isAsync) {
            this.async = isAsync;
            return this;
        }

        @Override
        public Builder<T> exceptionHandler(IHttpExceptionHandler handler) {
            httpExceptionHandler = handler;
            return this;
        }

        @Override
        public Builder<T> cacheStrategy(CacheStrategy cacheStrategy) {
            this.cacheStrategy = cacheStrategy;
            return this;
        }

        /**
         * 设置请求码
         *
         * @param requestCode
         * @return
         */
        @Override
        public Builder<T> requestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        /**
         * 设置请求call
         *
         * @param call
         * @return
         */
        @Deprecated
        public Builder<T> call(Call<T> call) {
            xCall = new XCall<>(call);
            return this;
        }

        public Builder<T> call(ICall<T> xCall) {
            this.xCall = xCall;
            return this;
        }

        /**
         * 设置请求监听
         *
         * @param httpRequestListener
         * @return
         */
        public Builder<T> listener(IHttpRequestListener httpRequestListener) {
            this.httpRequestListener = httpRequestListener;
            return this;
        }

        /**
         * 设置响应监听
         *
         * @param httpResponseListener
         * @return
         */
        public Builder<T> listener(IHttpResponseListener<T> httpResponseListener) {
            this.httpResponseListener = httpResponseListener;
            return this;
        }

        public Builder<T> listener(IHttpResponseCommonListener httpResponseCommonListener) {
            this.httpResponseCommonListener = httpResponseCommonListener;
            return this;
        }

        Context getContext() {
            return context;
        }

        IHttpRequestListener getHttpRequestListener() {
            return httpRequestListener;
        }

        IHttpResponseListener<T> getHttpResponseListener() {
            return httpResponseListenerProxy;
        }

    }

    private HttpRequester(Builder<T> builder) {
        this.mContext = builder.getContext();
        this.mCall = builder.xCall;
        this.mHttpRequestListener = builder.getHttpRequestListener();
        this.mHttpResponseListener = builder.getHttpResponseListener();
    }

    public static <API> API registerAPI(String baseUrl, Class<API> apiClass) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create()).build();
        API api = retrofit.create(apiClass);
        apis.put(apiClass, api);
        return api;
    }

    public static <API> API registerXAPI(String baseUrl, Class<API> apiClass) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(new XCallAdapterFactory()).build();
        API api = retrofit.create(apiClass);
        apis.put(apiClass, api);
        return api;
    }

    public static <API> API getAPI(Class<API> apiClass) {
        API api = (API) apis.get(apiClass);
        if (api == null) {
            throw new NullPointerException(apiClass + "的接口还未注册，请调用registerAPI进行api接口注册。");
        }
        return api;
    }

    private static void initDefaultOkHttpClient(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File(Util.getDiskFileDir(context), "okhttp");
        Cache cache = new Cache(cacheDirectory, cacheSize);
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).cache(cache);
        setOkHttpClient(builder);
    }

    public static void init(Context context) {
        initDefaultOkHttpClient(context.getApplicationContext());
        File cacheDirectory = new File(Util.getDiskFileDir(context), "okhttp");
        XCall.setDefaultCachingSystem(cacheDirectory);
    }

    public static void setDefaultCachingSystem(File cacheFile) {
        XCall.setDefaultCachingSystem(cacheFile);
    }

    public static void setOkHttpClient(OkHttpClient.Builder builder) {
        XCall.init(builder);
        mOkHttpClient = builder.build();
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            throw new NullPointerException("请先调用init()方法进行初始化");
        }
        return mOkHttpClient;
    }

    public static void setAuthTokenHandler(AuthTokenHandler handler) {
        XCall.setAuthTokenHandler(handler);
    }

    public static void setDefaultHttpExceptionHandler(
            IHttpExceptionHandler httpExceptionHandler) {
        XCall.setDefaultHttpExceptionHandler(httpExceptionHandler);
    }

    public static void setCacheStrategy(CacheStrategy cacheStrategy, int cacheStale) {
        XCall.setCacheStrategy(cacheStrategy, cacheStale);
    }

    public static void setDefaultHttpRequestListener(
            IHttpRequestListener httpRequestListener) {
        XCall.setDefaultHttpRequestListener(httpRequestListener);
    }

    @Deprecated
    public static <T> ICall<T> call(Call<T> call) {
        return new XCall<>(call);
    }

    public static <T> ICall<T> call(Retrofit retrofit, Call<T> call, Type responseType) {
        return new XCall<>(retrofit, call, responseType);
    }

    public static <T> ICallResponse<T> execute(Context context, ICall<T> call, IHttpResponseListener<T> listener) {
        return call.execute(context, listener);
    }

    @Deprecated
    public static <T> ICallResponse<T> execute(Context context, Call<T> call, IHttpResponseListener<T> listener) {
        return new HttpRequester.Builder<T>(context).call(call).listener(listener).request();
    }

    /**
     * 开始请求
     *
     * @return
     */
    public ICallResponse<T> request() {
        mCallResponse = mCall.execute(mContext, mHttpRequestListener, mHttpResponseListener);
        return mCallResponse;
    }

    /**
     * 取消本次请求
     */
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    /**
     * 获取本次请求的response
     * <p>
     * 1、异步操作的情况下，在未请求到数据的时候，此值会返回null。 2、同步操作的情况下，在请求结束后，此值会返回对应的内容
     *
     * @return
     */
    public Response getResponse() {
        if (mCallResponse != null) {
            return mCallResponse.getResponse();
        }
        return null;
    }

    /**
     * 获取返回的实体，如果未返回内容，则返回null
     *
     * @return
     */
    public T getResponseBody() {
        if (mCallResponse != null) {
            return mCallResponse.getResponseBody();
        }
        return null;
    }


}
