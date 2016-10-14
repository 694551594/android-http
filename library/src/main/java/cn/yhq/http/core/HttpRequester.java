package cn.yhq.http.core;

import android.content.Context;

import java.io.File;
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
    private IHttpRequestListener<T> mHttpRequestListener;
    private IHttpResponseListener<T> mHttpResponseListener;

    public static class Builder<T> {
        private IHttpRequestListener<T> httpRequestListener;
        private IHttpResponseListener<T> httpResponseListener;
        private IHttpResponseCommonListener httpResponseCommonListener;

        private Context context;
        private int requestCode;
        private CacheStrategy cacheStrategy = CacheStrategy.ONLY_NETWORK;
        private boolean async = true;
        private boolean exceptionProxy = true;

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
        }

        public HttpRequester<T> build() {
            if (this.xCall == null) {
                throw new NullPointerException("没有call对象？");
            }
            xCall.cacheStrategy(cacheStrategy);
            xCall.requestCode(requestCode);
            xCall.async(async);
            xCall.exceptionProxy(exceptionProxy);
            HttpRequester<T> httpRequester = new HttpRequester<>(this);
            return httpRequester;
        }

        public HttpRequester<T> request() {
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

        public Builder<T> exceptionProxy(boolean exceptionProxy) {
            this.exceptionProxy = exceptionProxy;
            return this;
        }

        /**
         * 设置请求的缓存策略
         *
         * @param cacheStrategy
         * @return
         */
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
        public Builder<T> listener(IHttpRequestListener<T> httpRequestListener) {
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

        IHttpRequestListener<T> getHttpRequestListener() {
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

    public static <T> void setDefaultHttpRequestListener(
            IHttpRequestListener<T> httpRequestListener) {
        XCall.setDefaultHttpRequestListener(httpRequestListener);
    }

    /**
     * 开始请求
     *
     * @return
     */
    public HttpRequester<T> request() {
        mCall.execute(mContext, mHttpRequestListener, mHttpResponseListener);
        return this;
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
        return mCall.getResponse();
    }

    /**
     * 获取返回的实体，如果未返回内容，则返回null
     *
     * @return
     */
    public T getResponseBody() {
        return mCall.getResponseBody();
    }


}
