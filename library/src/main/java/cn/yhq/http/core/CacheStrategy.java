package cn.yhq.http.core;

public enum CacheStrategy {
  
  // 仅仅请求缓存
  ONLY_CACHE,
  // 都有
  BOTH,
  // 仅仅请求网络
  ONLY_NETWORK,
  // 不缓存数据
  NOCACHE

}
