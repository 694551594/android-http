package cn.yhq.http.sample;

import cn.yhq.http.core.ICall;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Yanghuiqiang on 2016/10/10.
 */

public interface XAPI {

    @GET("weather_mini")
    ICall<WeatherInfo> getWeatherInfo(@Query("city") String city);
}
