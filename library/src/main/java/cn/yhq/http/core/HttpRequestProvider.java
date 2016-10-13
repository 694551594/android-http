package cn.yhq.http.core;


public abstract class HttpRequestProvider<T> implements IHttpRequestProvider<T> {

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return CacheStrategy.ONLY_NETWORK;
    }

}
