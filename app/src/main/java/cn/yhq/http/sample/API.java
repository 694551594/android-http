package cn.yhq.http.sample;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Yanghuiqiang on 2016/10/10.
 */

public interface API {

    @GET("weather_mini")
    Call<WeatherInfo> getWeatherInfo(@Query("city") String city);
}
