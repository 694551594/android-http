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
import cn.yhq.http.core.CacheStrategy;
import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.HttpResponseListener;
import cn.yhq.http.core.ICall;
import cn.yhq.http.core.ICallResponse;
import cn.yhq.http.core.IHttpRequestListener;
import cn.yhq.http.core.IHttpResponseListener;
import retrofit2.Call;

import static cn.yhq.http.core.HttpRequester.getAPI;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通常在Application中初始化
        HttpRequester.init(this);
        // 注册api接口
        HttpRequester.registerAPI("http://wthrcdn.etouch.cn", API.class);
        HttpRequester.registerXAPI("http://wthrcdn.etouch.cn", XAPI.class);
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

        setContentView(R.layout.activity_main);
        ListView listView = (ListView) this.findViewById(R.id.listview);

        final Context context = this;
        final IHttpResponseListener<WeatherInfo> httpResponseListener = new HttpResponseListener<WeatherInfo>() {
            @Override
            public void onResponse(Context context, int requestCode,
                                   WeatherInfo response, boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
                toast("isFromCache:" + isFromCache + "--" + new Gson().toJson(response));
            }

            @Override
            public void onException(Context context, Throwable t) {
                super.onException(context, t);
                // 自定义异常处理
                t.printStackTrace();
                // ToastUtils.showToast(context, t.getMessage());
            }

        };

        final SimpleStringListAdapter adapter =
                SimpleStringListAdapter.create(this, new String[]{
                        "普通异步请求",
                        "普通同步请求",
                        "XAPI直接异步请求",
                        "XAPI使用HttpRequester异步请求",
                        "XAPI直接同步请求",
                        "XAPI使用HttpRequester同步请求",
                        "使用自定义请求监听器",
                        "最简单的请求方式"
                });

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Call<WeatherInfo> call = getAPI(API.class).getWeatherInfo("北京");
                final ICall<WeatherInfo> xCall = getAPI(XAPI.class).getWeatherInfo("北京");
                switch (position) {
                    case 0:
                        // 普通的call异步请求
                        ICallResponse<WeatherInfo> callResponse =
                                new HttpRequester.Builder<WeatherInfo>(context)
                                        .call(call)
                                        .listener(httpResponseListener)
                                        .request();
                        // callResponse.cancel();
                        break;
                    case 1:
                        // 普通的call同步请求
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ICallResponse<WeatherInfo> callResponse =
                                        new HttpRequester.Builder<WeatherInfo>(context)
                                                .call(call)
                                                .setAsync(false)
                                                .listener(httpResponseListener)
                                                .request();
                                WeatherInfo weatherInfo = callResponse.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 2:
                        // xcall异步请求1
                        xCall.cacheStrategy(CacheStrategy.REQUEST_FAILED_READ_CACHE)
                                .execute(context, httpResponseListener);
                        break;
                    case 3:
                        // xcall异步请求2
                        new HttpRequester.Builder<WeatherInfo>(context)
                                .call(xCall)
                                .cacheStrategy(CacheStrategy.FIRST_CACHE_THEN_REQUEST)
                                .listener(httpResponseListener)
                                .request();
                        break;
                    case 4:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // xcall同步请求1
                                ICallResponse<WeatherInfo> callResponse = xCall.async(false)
                                        .execute(context, httpResponseListener);
                                WeatherInfo weatherInfo = callResponse.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 5:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // xcall同步请求2
                                ICallResponse<WeatherInfo> callResponse = new HttpRequester.Builder<WeatherInfo>(context)
                                        .call(xCall)
                                        .sync()
                                        .listener(httpResponseListener)
                                        .request();
                                WeatherInfo weatherInfo = callResponse.getResponseBody();
                                System.out.println(weatherInfo);
                            }
                        }).start();
                        break;
                    case 6:
                        new HttpRequester.Builder<WeatherInfo>(context)
                                .call(xCall)
                                .listener((IHttpRequestListener) null)
                                .listener(httpResponseListener).request();
                        break;
                    case 7:
                        HttpRequester.execute(context, xCall, httpResponseListener);
                        break;
                }

            }
        });

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
