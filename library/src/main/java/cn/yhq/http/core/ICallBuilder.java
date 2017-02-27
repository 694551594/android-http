package cn.yhq.http.core;

/**
 * Created by Yanghuiqiang on 2016/12/26.
 */

public interface ICallBuilder<T> {

    T requestCode(int requestCode);

    T async(boolean isAsync);

    T exceptionHandler(IHttpExceptionHandler handler);

    T cacheStrategy(CacheStrategy cacheStrategy);
}
