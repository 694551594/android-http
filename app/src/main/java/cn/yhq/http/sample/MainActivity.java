package cn.yhq.http.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import cn.yhq.adapter.list.SimpleStringListAdapter;
import cn.yhq.http.core.AuthTokenHandler;
import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.HttpResponseListener;
import cn.yhq.http.core.ICall;
import cn.yhq.http.core.IHttpRequestListener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) this.findViewById(R.id.listview);

        final SimpleStringListAdapter adapter =
                SimpleStringListAdapter.create(this, new String[]{
                        "普通异步请求",
                        "普通同步请求",
                        "XAPI直接异步请求",
                        "XAPI使用HttpRequester异步请求",
                        "XAPI直接同步请求",
                        "XAPI使用HttpRequester同步请求",
                        "使用自定义请求监听器"
                });

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 普通的call异步请求
                        HttpRequester<WeatherInfo> httpRequester =
                                new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                                        .call(HttpRequester.getAPI(API.class).getWeatherInfo("北京")).exceptionProxy(false)
                                        .listener(new HttpResponseListener<WeatherInfo>() {
                                            @Override
                                            public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                                   boolean isFromCache) {
                                                super.onResponse(context, requestCode, response, isFromCache);
                                                toast(new Gson().toJson(response));
                                            }

                                            @Override
                                            public void onException(Context context, Throwable t) {
                                                super.onException(context, t);
                                                // 自定义异常处理
                                            }
                                        }).request();
                        // httpRequester.cancel();
                        break;
                    case 1:
                        // 普通的call同步请求
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                HttpRequester<WeatherInfo> httpRequester =
                                        new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                                                .call(HttpRequester.getAPI(API.class).getWeatherInfo("北京")).setAsync(false)
                                                .listener(new HttpResponseListener<WeatherInfo>() {
                                                    @Override
                                                    public void onResponse(Context context, int requestCode,
                                                                           WeatherInfo response, boolean isFromCache) {
                                                        super.onResponse(context, requestCode, response, isFromCache);
                                                        toast(new Gson().toJson(response));
                                                    }
                                                }).request();
                                WeatherInfo weatherInfo = httpRequester.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 2:
                        // xcall异步请求1
                        HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京").execute(MainActivity.this, new HttpResponseListener<WeatherInfo>() {
                            @Override
                            public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                   boolean isFromCache) {
                                super.onResponse(context, requestCode, response, isFromCache);
                                toast(new Gson().toJson(response));
                            }

                        });
                        break;
                    case 3:
                        // xcall异步请求2
                        new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                                .call(HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京"))
                                .listener(new HttpResponseListener<WeatherInfo>() {
                                    @Override
                                    public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                           boolean isFromCache) {
                                        super.onResponse(context, requestCode, response, isFromCache);
                                        toast(new Gson().toJson(response));
                                    }

                                    @Override
                                    public void onException(Context context, Throwable t) {
                                        super.onException(context, t);
                                        // 自定义异常处理
                                    }
                                }).request();
                        break;
                    case 4:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // xcall同步请求1
                                ICall<WeatherInfo> call = HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京").async(false).execute(MainActivity.this, new HttpResponseListener<WeatherInfo>() {
                                    @Override
                                    public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                           boolean isFromCache) {
                                        super.onResponse(context, requestCode, response, isFromCache);
                                        toast(new Gson().toJson(response));
                                    }

                                });
                                WeatherInfo weatherInfo = call.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 5:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // xcall同步请求2
                                HttpRequester<WeatherInfo> httpRequester = new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                                        .call(HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京"))
                                        .sync()
                                        .listener(new HttpResponseListener<WeatherInfo>() {
                                            @Override
                                            public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                                   boolean isFromCache) {
                                                super.onResponse(context, requestCode, response, isFromCache);
                                                toast(new Gson().toJson(response));
                                            }
                                        }).request();
                                WeatherInfo weatherInfo = httpRequester.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 6:
                        new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                                .call(HttpRequester.getAPI(XAPI.class).getWeatherInfo("北京"))
                                .listener((IHttpRequestListener) null)
                                .listener(new HttpResponseListener<WeatherInfo>() {
                                    @Override
                                    public void onResponse(Context context, int requestCode, WeatherInfo response,
                                                           boolean isFromCache) {
                                        super.onResponse(context, requestCode, response, isFromCache);
                                        toast(new Gson().toJson(response));
                                    }
                                }).request();
                        break;
                }

            }
        });

        // 通常在Application中初始化
        HttpRequester.init(this);
        // token验证处理器
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
        // 注册api接口
        HttpRequester.registerAPI("http://wthrcdn.etouch.cn", API.class);
        HttpRequester.registerXAPI("http://wthrcdn.etouch.cn", XAPI.class);
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }

    String getAccessToken() {
        return null;
    }

    void startLoginActivity() {

    }
}
