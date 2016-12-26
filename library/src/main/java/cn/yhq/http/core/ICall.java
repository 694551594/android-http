package cn.yhq.http.core;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

public interface ICall<T> extends ICallExecutor<T> {

    ICall<T> requestCode(int requestCode);

    ICall<T> cacheStrategy(CacheStrategy cacheStrategy);

    ICall<T> async(boolean isAsync);

    ICall<T> cacheStale(int cacheStale);

    ICall<T> exceptionProxy(boolean exceptionProxy);

}
