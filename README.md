# android-http

此框架是基于retrofit2+okhttp3封装的http请求框架。主要集成了接口注册、权限处理、缓存处理、请求过程的监听、请求回复的监听、请求异常处理等功能，增加了请求数据的时候显示进度对话框的功能。

####注意：此框架仅适用于使用了retrofit2+okhttp3作为http请求的应用。

#gradle配置
`compile 'cn.yhq:android-http:2.4'`

#V2.0版本新的请求方式
此次更新主要将封装代码转移到新的XCall类里面，然后创建了retrofit2的Call适配器XCallAdapterFactory，扩展了请求数据的方式。
其他API接口不变，我们只说新增加的地方：
###1、编写API接口
retrofit2最大的特色啦，把一个http请求使用注解的方法去调用，我们这里以获取天气预报的接口为例，注意，和原来不同的是，方法的返回参数为ICall：
```java
public interface XAPI {

    @GET("weather_mini")
    ICall<WeatherInfo> getWeatherInfo(@Query("city") String city);
}

```

###2、注册接口
使用HttpRequester将上一步写的接口注册进来，注意，和原来不同的是使用了registerXAPI方法：
```java
HttpRequester.registerXAPI("http://wthrcdn.etouch.cn", XAPI.class);
```

###3、请求数据
（1）直接使用ICall对象请求：调用ICall的execute方法即可：
```java
HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京").execute(MainActivity.this, new HttpResponseListener<WeatherInfo>() {
    @Override
    public void onResponse(Context context, int requestCode, WeatherInfo response,
                           boolean isFromCache) {
        super.onResponse(context, requestCode, response, isFromCache);
    }
});
```
（2）构建HttpRequester请求器：
```java
new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
        .call(HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京"))
        .listener(new HttpResponseListener<WeatherInfo>() {
            @Override
            public void onResponse(Context context, int requestCode, WeatherInfo response,
                                   boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
            }

            @Override
            public void onException(Context context, Throwable t) {
                super.onException(context, t);
            }
        }).request();
```
其他API的使用方法和之前的一样。

#使用方式
###1、初始化
在你的Application类里面初始化：
```java
// 通常在Application中初始化
HttpRequester.init(this);
```
初始化后okhttp的缓存位置为应用的缓存位置\okhttp文件夹下，缓存时间为7天，如果你想自定义这些参数，可以自定义okhttpclient，参考下一步。
###2、自定义okhttpclient
HttpRequester中初始化了默认的okhttpclient，如果你想自定义okhttpclient，可以调用setOkHttpClient方法设置：
```java
HttpRequester.setOkHttpClient(OkHttpClient.Builder builder)
```
###3、设置token验证处理器
现在很多api接口都使用了权限保护，主要有token和cookie验证。这里我们可以注册一个token验证处理器，然后返回权限头的名称和权限头的值，比如token的头"Authorization":"Bearer token值",cookie的头"Authorization":"cookie值"。

getAuthValue方法中的isRefresh参数代表当前是否需要刷新token或者cookie，当isRefresh=true的时候，代表需要重新登录，这个时候我们可以进入登录的界面，让用户重新登录。

```java
HttpRequester.setAuthTokenHandler(new AuthTokenHandler() {
      @Override
      public String getAuthName() {
        // 获取权限验证的key
        return "Authorization";
      }

      @Override
      public String getAuthValue(boolean isRefresh) {
        // 获取权限验证的值，如果isRefresh为true的话，说明需要重新进行验证了
        if (isRefresh) {
          // 进入登录界面
          startLoginActivity();
          return null;
        } else {
          // 返回token
          return "Bearer " + getAccessToken();
        }
      }

      @Override
      public boolean isIgnoreUrl(String url) {
        // 忽略掉，不需要权限验证的url
        return false;
      }
    });
```

###4、编写API接口
retrofit2最大的特色啦，把一个http请求使用注解的方法去调用，我们这里以获取天气预报的接口为例：
```java
public interface API {

    @GET("weather_mini")
    Call<WeatherInfo> getWeatherInfo(@Query("city") String city);
}

```

###5、注册接口
使用HttpRequester将第四步的接口注册进来：
```java
HttpRequester.registerAPI("http://wthrcdn.etouch.cn", API.class);
```

###6、请求数据
```java
HttpRequester<WeatherInfo> httpRequester =
    new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
        .call(getAPI().getWeatherInfo("北京"))
        .listener(new HttpResponseListener<WeatherInfo>() {
          @Override
          public void onResponse(Context context, int requestCode, WeatherInfo response,
              boolean isFromCache) {
            super.onResponse(context, requestCode, response, isFromCache);
          }
        }).request();
```
ok,数据请求成功。

取消请求的方法：
```java
httpRequester.cancel();
```

###7、同步请求
本库也支持同步请求，同步请求适合在线程里面请求数据。在构建请求器的时候设置``.setAsync(false)``即可。
```java
 new Thread(new Runnable() {
      @Override
      public void run() {
        HttpRequester<WeatherInfo> httpRequester =
            new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                .call(getAPI().getWeatherInfo("北京")).setAsync(false)
                .listener(new HttpResponseListener<WeatherInfo>() {
                  @Override
                  public void onResponse(Context context, int requestCode,
                      WeatherInfo response, boolean isFromCache) {
                    super.onResponse(context, requestCode, response, isFromCache);
                    // UI线程
                  }
                }).request();
        // IO线程
        WeatherInfo weatherInfo = httpRequester.getResponseBody();
        System.out.println(weatherInfo);
      }
 }).start();
```

这里要说明的是：同步请求获取返回数据有两种方式，
（1）调用``httpRequester.getResponseBody()``直接返回响应实体，此操作一般在IO线程里面使用。
（2）添加HttpResponseListener监听器进行回调，在回调参数里面可以拿到实体，此回调方法运行在UI线程里。所以大家可以根据实际情况去选择拿到实体的方式。

###8、自定义请求监听器
（1）如果你想自定义请求的时候展现的请求对话框，你需要调用HttpRequester.setDefaultHttpRequestListener(IHttpRequestListener<T> httpRequestListener)方法设置你自己的请求监听器：
```java
HttpRequester.setDefaultHttpRequestListener(new IHttpRequestListener<T>() {
    private IDialog mLoadingDialog;
       
    @Override
    public void onStart(Context context, final ICancelable cancelable, int requestCode) {
        this.mCancelable = cancelable;
        mLoadingDialog = DialogBuilder.loadingDialog(context).setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelable.cancel();
            }
        }).show();
    }

    @Override
    public void onComplete(int requestCode) {
        mLoadingDialog.dismiss();
    }

});
```

（2）当然，如果你在单次请求中不想使用请求监听器，你可以在构建HttpRequester的时候，将IHttpRequestListener设置为null即可，其他请求不会受影响：
```java
HttpRequester<WeatherInfo> httpRequester =
    new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
        .call(getAPI().getWeatherInfo("北京"))
        .listener((IHttpRequestListener<T>)null)
        .listener(new HttpResponseListener<WeatherInfo>() {
              @Override
              public void onResponse(Context context, int requestCode, WeatherInfo response,
                  boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
              }
    
              @Override
              public void onException(Context context, Throwable t) {
                super.onException(context, t);
                // 单次请求异常处理
              }
        }).request();
```

###9、自定义异常处理
异常处理分为全局异常与单次请求异常。

（1）全局异常处理器：IHttpExceptionHandler，负责处理比如无网络，请求超时等等异常。全局异常处理器在每次请求发生异常的时候都会去调用处理。如果你想自定义全局异常处理器，需要调用HttpRequester.setDefaultHttpExceptionHandler(IHttpExceptionHandler httpExceptionHandler)方法：
```java
HttpRequester.setDefaultHttpExceptionHandler(new IHttpExceptionHandler() {
 @Override
    public void onException(Context context, Throwable t) {
    }
});
```
（2）单次请求异常处理：在构建HttpRequester的时候，可以添加HttpResponseListener监听器，此监听器其中的onException方法就是此次请求的异常处理回调，如果有需要，你可以在此方法做一些处理：
```java
HttpRequester<WeatherInfo> httpRequester =
    new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
        .call(getAPI().getWeatherInfo("北京"))
        .listener(new HttpResponseListener<WeatherInfo>() {
              @Override
              public void onResponse(Context context, int requestCode, WeatherInfo response,
                  boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
              }
    
              @Override
              public void onException(Context context, Throwable t) {
                super.onException(context, t);
                // 单次请求异常处理
              }
        }).request();
```
（3）禁用全局异常处理器：如果你某一次请求不希望使用全局异常处理器处理此次异常，你可以设置exceptionProxy=false来禁用此次的全局异常处理，其他请求不受影响。
```java
new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
    .call(HttpRequester.getAPI(API.class).getWeatherInfo("北京"))
    .exceptionProxy(false)
    .listener(new HttpResponseListener<WeatherInfo>() {
        @Override
        public void onResponse(Context context, int requestCode, WeatherInfo response,
                               boolean isFromCache) {
            super.onResponse(context, requestCode, response, isFromCache);
        }

        @Override
        public void onException(Context context, Throwable t) {
            super.onException(context, t);
            // 单次请求异常处理
        }
}).request();
```
