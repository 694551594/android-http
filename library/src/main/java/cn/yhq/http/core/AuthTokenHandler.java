package cn.yhq.http.core;


/**
 * Created by Yanghuiqiang on 2016/8/10.
 */
public interface AuthTokenHandler {

  String getAuthName();

  String getAuthValue(boolean isRefresh);

  boolean isIgnoreUrl(String url);

}
