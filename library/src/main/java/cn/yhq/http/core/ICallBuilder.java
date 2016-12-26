package cn.yhq.http.core;

/**
 * Created by Yanghuiqiang on 2016/12/26.
 */

public interface ICallBuilder<T> {

    T requestCode(int requestCode);

    T cacheStrategy(CacheStrategy cacheStrategy);

    T async(boolean isAsync);

    T cacheStale(int cacheStale);

    T exceptionProxy(boolean exceptionProxy);

}
