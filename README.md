# android-http

此框架是基于retrofit2+okhttp3封装的http请求框架。主要集成了接口注册、权限处理、缓存处理、请求过程的监听、请求回复的监听、请求异常处理等功能，增加了请求数据的时候显示进度对话框的功能。

####注意：此框架仅适用于使用了retrofit2+okhttp3作为http请求的应用。

#gradle配置
`compile 'cn.yhq:android-http:1.1'`

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
            Toast.makeText(context, new Gson().toJson(response), Toast.LENGTH_LONG)
                .show();
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
                            Toast.makeText(context, new Gson().toJson(response), Toast.LENGTH_LONG)
                                    .show();
                          }
                        }).request();
                // IO线程
                WeatherInfo weatherInfo = httpRequester.getResponseBody();
                System.out.println(weatherInfo);
              }
            }).start();
```

这里要说明的是：同步请求获取返回数据有两种方式，1、调用``httpRequester.getResponseBody()``直接返回响应实体，此操作一般在IO线程里面使用。2、添加HttpResponseListener监听器进行回调，在回调参数里面可以拿到实体，此回调方法运行在UI线程里。所以大家可以根据实际情况去选择拿到实体的方式。

###8、自定义异常处理
此框架中自带了异常处理器，如果你想自定义异常处理器，可以设置exceptionProxy=false：

```java
HttpRequester<WeatherInfo> httpRequester =
    new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
        .call(getAPI().getWeatherInfo("北京")).exceptionProxy(false)
        .listener(new HttpResponseListener<WeatherInfo>() {
          @Override
          public void onResponse(Context context, int requestCode, WeatherInfo response,
              boolean isFromCache) {
            super.onResponse(context, requestCode, response, isFromCache);
            Toast.makeText(context, new Gson().toJson(response), Toast.LENGTH_LONG)
                .show();
          }

          @Override
          public void onException(Context context, Throwable t) {
            super.onException(context, t);
            // 自定义异常处理
          }
        }).request();
```

这样，你就可以自己处理异常情况了。
