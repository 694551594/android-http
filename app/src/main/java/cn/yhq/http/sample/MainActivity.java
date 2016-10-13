package cn.yhq.http.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import cn.yhq.adapter.core.ViewHolder;
import cn.yhq.adapter.list.SimpleListAdapter;
import cn.yhq.http.core.AuthTokenHandler;
import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.HttpResponseListener;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ListView listView = (ListView) this.findViewById(R.id.listview);

    final SimpleListAdapter<String> adapter =
        SimpleListAdapter.create(this, new String[] {"普通异步请求", "普通同步请求", "文件上传", "文件下载"},
            android.R.layout.simple_list_item_1, new SimpleListAdapter.IItemViewSetup<String>() {
              @Override
              public void setupView(ViewHolder viewHolder, int position, String entity) {
                viewHolder.bindTextData(android.R.id.text1, entity);
              }
            });

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            HttpRequester<WeatherInfo> httpRequester =
                new HttpRequester.Builder<WeatherInfo>(MainActivity.this)
                    .call(getAPI().getWeatherInfo("北京")) // .exceptionProxy(false)
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
            // httpRequester.cancel();
            break;
          case 1:
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
                            Toast.makeText(context, new Gson().toJson(response), Toast.LENGTH_LONG)
                                    .show();
                          }
                        }).request();
                WeatherInfo weatherInfo = httpRequester.getResponseBody();
                System.out.println(weatherInfo);
              }
            }).start();
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
  }

  API getAPI() {
    return HttpRequester.getAPI(API.class);
  }

  String getAccessToken() {
    return null;
  }

  void startLoginActivity() {

  }
}
