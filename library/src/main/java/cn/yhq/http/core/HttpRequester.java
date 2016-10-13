package cn.yhq.http.core;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * http请求，使用了okhttp3以及retrofit2
 * 
 * @author Yanghuiqiang 2015-10-9
 * 
 * @param <T>
 */
public final class HttpRequester<T> {
  private final static String TAG = "HttpRequester";
  // 拦截器
  private final static HttpRequestCacheInterceptor mHttpRequestCacheInterceptor =
      new HttpRequestCacheInterceptor();
  private final static HttpResponseProgressInterceptor mHttpResponseProgressInterceptor =
      new HttpResponseProgressInterceptor();
  private final static HttpRequestProgressInterceptor mHttpRequestProgressInterceptor =
      new HttpRequestProgressInterceptor();
  private final static AuthenticatorInterceptor mAuthenticatorInterceptor =
      new AuthenticatorInterceptor();
  private final static AuthTokenInterceptor mAuthTokenInterceptor = new AuthTokenInterceptor();

  private final static Map<Context, List<HttpRequester<?>>> mHttpRequester = new HashMap<>();
  private final static Map<Class<?>, Object> apis = new HashMap<>();
  private static OkHttpClient mOkHttpClient;
  private static AuthTokenHandler mAuthTokenHandler;
  // 缓存有效时间
  private final static int CACHE_MAX_STALE = 7 * 24 * 3600;

  private retrofit2.Response<T> mResponse;
  private Call<T> mCall;
  private Context mContext;
  private boolean isAsync;
  private IHttpRequestProvider mHttpRequestProvider;
  private IHttpRequestListener mHttpRequestListener;
  private IHttpResponseListener mHttpResponseListener;
  private HttpHandler mHttpHandler;

  public static class Builder<T> {
    private Context context;
    private IHttpRequestProvider<T> httpRequestProvider;
    private IHttpRequestListener<T> httpRequestListener;
    private IHttpResponseListener<T> httpResponseListener;
    private IHttpResponseCommonListener httpResponseCommonListener;
    private IHttpResponseListener<T> httpResponseExceptionListener =
        new HttpResponseExceptionListener<T>();

    private final HttpRequestProvider<T> _httpRequestProvider = new HttpRequestProvider<T>() {
      @Override
      public Call<T> execute(int requestCode) {
        return null;
      }
    };

    private Call<T> call;
    private int requestCode = _httpRequestProvider.getRequestCode();
    private CacheStrategy cacheStrategy = _httpRequestProvider.getCacheStrategy();
    private boolean exceptionProxy = true;
    private boolean async = true;

    private IHttpResponseListener httpResponseListenerProxy = new HttpResponseListener<T>() {

      @Override
      public void onResponse(Context context, int requestCode, T response, boolean isFromCache) {
        super.onResponse(context, requestCode, response, isFromCache);
        if (exceptionProxy) {
          httpResponseExceptionListener.onResponse(context, requestCode, response, isFromCache);
        }
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
        if (exceptionProxy) {
          httpResponseExceptionListener.onException(context, t);
        }
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
      if (!this.isAsync()) {
        this.httpRequestListener = null;
      } else {
        if (this.httpRequestListener == null) {
          this.httpRequestListener = new DefaultHttpRequestListener<>(context);
        }
      }
      if (this.httpRequestProvider == null) {
        this.httpRequestProvider = new IHttpRequestProvider<T>() {

          @Override
          public int getRequestCode() {
            return requestCode;
          }

          @Override
          public Call<T> execute(int requestCode) {
            return call;
          }

          @Override
          public CacheStrategy getCacheStrategy() {
            return cacheStrategy;
          }

        };
      }
      HttpRequester<T> httpRequester = new HttpRequester<T>(this);
      return httpRequester;
    }

    public HttpRequester<T> request() {
      HttpRequester<T> httpRequester = build();
      return httpRequester.request();
    }

    public Builder<T> provider(IHttpRequestProvider<T> httpRequestProvider) {
      this.httpRequestProvider = httpRequestProvider;
      return this;
    }

    /**
     * 设置是否由系统代理异常的处理，如果false，则出现异常不会提示，需要用户自己处理。 默认为true
     *
     * @param exceptionProxy
     * @return
     */
    public Builder<T> exceptionProxy(boolean exceptionProxy) {
      this.exceptionProxy = exceptionProxy;
      return this;
    }

    public boolean isAsync() {
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
      this.call = call;
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

    IHttpRequestProvider<T> getHttpRequestProvider() {
      return httpRequestProvider;
    }

    IHttpRequestListener<T> getHttpRequestListener() {
      return httpRequestListener;
    }

    IHttpResponseListener<T> getHttpResponseListener() {
      return httpResponseListenerProxy;
    }

  }

  private HttpRequester(Builder builder) {
    this.mContext = builder.getContext();
    this.mHttpRequestListener = builder.getHttpRequestListener();
    this.mHttpRequestProvider = builder.getHttpRequestProvider();
    this.mHttpResponseListener = builder.getHttpResponseListener();
    this.isAsync = builder.isAsync();
    this.mHttpHandler =
        new HttpHandler(this.mContext, this.mHttpRequestListener, this.mHttpResponseListener);
    // 进度监听
    mHttpResponseProgressInterceptor.setProgressListener(new ProgressListener() {

      @Override
      public void update(boolean multipart, long bytesRead, long contentLength, boolean done) {
        mHttpHandler.responseProgress(multipart, bytesRead, contentLength, done);
      }
    });
    mHttpRequestProgressInterceptor.setProgressListener(new ProgressListener() {

      @Override
      public void update(boolean multipart, long bytesRead, long contentLength, boolean done) {
        mHttpHandler.requestProgress(multipart, bytesRead, contentLength, done);
      }
    });
  }

  public static <API> API registerAPI(String baseUrl, Class<API> apiClass) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build();
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
    File cacheDirectory = new File(getDiskFileDir(context), "okhttp");
    Cache cache = new Cache(cacheDirectory, cacheSize);
    OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).cache(cache);
    setOkHttpClient(builder);
  }

  public static void init(Context context) {
    initDefaultOkHttpClient(context.getApplicationContext());
  }

  private static String getDiskFileDir(Context context) {
    String cachePath = null;
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
            || !Environment.isExternalStorageRemovable()) {
      cachePath = context.getExternalFilesDir(null).getPath();
    } else {
      cachePath = context.getFilesDir().getPath();
    }
    return cachePath;
  }

  public static void setAuthTokenHandler(AuthTokenHandler handler) {
    mAuthTokenHandler = handler;
  }

  public static void setOkHttpClient(OkHttpClient.Builder builder) {
    builder.addInterceptor(mHttpRequestCacheInterceptor)
        .addInterceptor(mHttpRequestProgressInterceptor)
        .addInterceptor(mHttpResponseProgressInterceptor).authenticator(mAuthenticatorInterceptor)
        .addNetworkInterceptor(mAuthTokenInterceptor);
    mOkHttpClient = builder.build();
  }

  public static OkHttpClient getOkHttpClient() {
    if (mOkHttpClient == null) {
      throw new NullPointerException("请先调用init()方法进行初始化");
    }
    return mOkHttpClient;
  }

  /**
   * 当验证失败的时候处理的一个拦截器
   *
   */
  private static class AuthenticatorInterceptor implements Authenticator {

    @Override
    public Request authenticate(Route route, final Response response) throws IOException {
      if (mAuthTokenHandler == null) {
        return null;
      }
      String authValue = mAuthTokenHandler.getAuthValue(true);
      if (authValue == null) {
        return null;
      }
      return response.request().newBuilder().addHeader(mAuthTokenHandler.getAuthName(), authValue)
          .build();
    }
  }

  /**
   * 负责给每一个请求添加token
   *
   */
  private static class AuthTokenInterceptor implements Interceptor {

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
      Request originalRequest = chain.request();
      if (mAuthTokenHandler == null) {
        return chain.proceed(originalRequest);
      }
      if (mAuthTokenHandler.isIgnoreUrl(originalRequest.url().toString())) {
        return chain.proceed(originalRequest);
      }
      String authValue = mAuthTokenHandler.getAuthValue(false);
      if (authValue == null || alreadyHasAuthorizationHeader(originalRequest)) {
        return chain.proceed(originalRequest);
      }
      Request authorised =
          originalRequest.newBuilder().header(mAuthTokenHandler.getAuthName(), authValue).build();
      return chain.proceed(authorised);
    }

    private static boolean alreadyHasAuthorizationHeader(Request originalRequest) {
      return originalRequest.header(mAuthTokenHandler.getAuthName()) != null;
    }

  }

  /**
   * 对response进行进度监听的拦截器
   * 
   * @author Yanghuiqiang
   * 
   *         2016-1-15
   */
  private static class HttpResponseProgressInterceptor implements Interceptor {
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
      this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Response originalResponse = chain.proceed(chain.request());
      return originalResponse.newBuilder()
          .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
    }

  }

  /**
   * 对request进度监听的拦截器
   * 
   * @author Yanghuiqiang
   * 
   *         2016-1-15
   */
  private static class HttpRequestProgressInterceptor implements Interceptor {
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
      this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      if (request.body() != null) {
        request = request.newBuilder()
            .post(new ProgressRequestBody(request.body(), progressListener)).build();
      }
      return chain.proceed(request);
    }

  }

  /**
   * 自定义缓存处理的拦截器
   *
   */
  private static class HttpRequestCacheInterceptor implements Interceptor {
    private CacheStrategy cacheStrategy;

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
      this.cacheStrategy = cacheStrategy;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      String cacheHeaderName = "Cache-Control";
      String cacheHeaderValue = "max-stale=" + CACHE_MAX_STALE;
      switch (cacheStrategy) {
        case BOTH:
          break;
        case NOCACHE:
          cacheHeaderValue = "no-cache";
          break;
        case ONLY_CACHE:
          cacheHeaderValue = "only-if-cached";
          break;
        case ONLY_NETWORK:
          cacheHeaderValue = "no-cache";
          break;
        default:
          break;
      }
      request = request.newBuilder().removeHeader(cacheHeaderName)
          .addHeader(cacheHeaderName, cacheHeaderValue).build();
      Response response = chain.proceed(request);
      return response;
    }

  }

  /**
   * 开始请求
   *
   * @return
   */
  public HttpRequester<T> request() {
    return handleRequest();
  }

  /**
   * 取消本次请求
   *
   */
  public void cancel() {
    if (mCall != null) {
      mCall.cancel();
    }
  }

  public static void cancel(Context context) {
    List<HttpRequester<?>> httpRequesterList = mHttpRequester.get(context);
    if (httpRequesterList != null) {
      for (HttpRequester<?> httpRequester : httpRequesterList) {
        httpRequester.cancel();
      }
      mHttpRequester.remove(context);
    }
  }

  private static void put(Context context, HttpRequester<?> httpRequester) {
    List<HttpRequester<?>> list = mHttpRequester.get(context);
    if (list == null) {
      list = new ArrayList<>();
      mHttpRequester.put(context, list);
    }
    list.add(httpRequester);
  }

  private static void remove(Context context, HttpRequester<?> httpRequester) {
    List<HttpRequester<?>> list = mHttpRequester.get(context);
    if (list != null) {
      list.remove(httpRequester);
      if (list.isEmpty()) {
        mHttpRequester.remove(context);
      }
    }
  }

  /**
   * 获取本次请求的response
   *
   * 1、异步操作的情况下，在未请求到数据的时候，此值会返回null。
   * 2、同步操作的情况下，在请求结束后，此值会返回对应的内容
   *
   * @return
   */
  public retrofit2.Response getResponse() {
    return mResponse;
  }

  /**
   * 获取返回的实体，如果未返回内容，则返回null
   *
   * @return
     */
  public T getResponseBody() {
    if (mResponse == null) {
      return null;
    }
    return mResponse.body();
  }

  /**
   * 缓存处理
   * 
   * @param cacheStrategy
   */
  private void handleCache(final CacheStrategy cacheStrategy) {
    mHttpRequestCacheInterceptor.setCacheStrategy(cacheStrategy);
  }

  /**
   * 请求处理
   *
   * @return
   */
  private HttpRequester<T> handleRequest() {
    put(mContext, this);
    handleCache(this.mHttpRequestProvider.getCacheStrategy());
    final int requestCode = this.mHttpRequestProvider.getRequestCode();
    try {
      // 回调处理
      Callback<T> callback = new Callback<T>() {

        @Override
        public void onResponse(Call<T> call, retrofit2.Response<T> response) {
          remove(mContext, HttpRequester.this);
          mResponse = response;
          mHttpHandler.responseSuccess(response, requestCode);
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
          remove(mContext, HttpRequester.this);
          mHttpHandler.responseException(t, requestCode);
        }
      };
      Call<T> call = this.mHttpRequestProvider.execute(requestCode);
      if (call == null) {
        throw new NullPointerException("没有指定call。");
      }
      mCall = call;
      mHttpHandler.requestStart(call, requestCode);
      // 真正开始请求的地方
      if (this.isAsync) {
        // 异步执行
        call.enqueue(callback);
      } else {
        // 同步执行
        callback.onResponse(call, call.execute());
      }
    } catch (Throwable t) {
      remove(mContext, HttpRequester.this);
      mHttpHandler.requestException(t, requestCode);
    }
    return this;
  }

}
