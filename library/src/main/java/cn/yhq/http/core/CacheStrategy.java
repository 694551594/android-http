package cn.yhq.http.core;

public enum CacheStrategy {
    @Deprecated
    BOTH,
    // 仅仅请求缓存
    ONLY_CACHE,
    // 都有
    FIRST_CACHE_THEN_REQUEST,
    // 仅仅请求网络
    ONLY_NETWORK,
    // 不缓存数据
    NOCACHE,
    /**
     * 请求网络失败后，读取缓存
     */
    REQUEST_FAILED_READ_CACHE,
    /**
     * 如果缓存不存在才请求网络，否则使用缓存
     */
    IF_NONE_CACHE_REQUEST

}
