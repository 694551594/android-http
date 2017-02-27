package cn.yhq.http.core.cache;


import okhttp3.Request;
import retrofit2.Response;

public interface CachingSystem {
    <T> void addInCache(Response<T> response);

    byte[] getFromCache(Request request);
}
