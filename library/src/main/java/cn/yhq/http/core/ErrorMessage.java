package cn.yhq.http.core;

/**
 * 错误信息
 * 
 * @author Yanghuiqiang 2014-9-20
 *
 */
public class ErrorMessage {
  public int code;
  public String msg;

  public ErrorMessage(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }
  
  public final static int ERR_CODE_HTTP_NOCONNECTION = 1;
  public final static int ERR_CODE_HTTP_REFUSEED = 2;
  public final static int ERR_CODE_HTTP_SERVICEINNER = 3;
  // 此错误不会出现，只供调试使用，若出现则是编码错误
  public final static int ERR_CODE_HTTP_RESPONSEPARSER = 4;
  public final static int ERR_CODE_HTTP_SERVICECONNECTION = 5;
  
  
  
  /** 网络连接失败 **/
  public final static ErrorMessage ERR_HTTP_NOCONNECTION = new ErrorMessage(ERR_CODE_HTTP_NOCONNECTION, "服务器连接失败，请检测网络是否畅通");
  /** 服务器连接超时**/
  public final static ErrorMessage ERR_HTTP_REFUSEED = new ErrorMessage(ERR_CODE_HTTP_REFUSEED, "连接服务器超时，请稍后重试");
  /** 服务器内部错误**/
  public final static ErrorMessage ERR_HTTP_SERVICEINNER = new ErrorMessage(ERR_CODE_HTTP_SERVICEINNER, "服务器内部错误，请稍后重试");
  /** 数据解析错误 **/
  public final static ErrorMessage ERR_HTTP_RESPONSEPARSER = new ErrorMessage(ERR_CODE_HTTP_RESPONSEPARSER, "响应数据解析错误");
  
  public final static ErrorMessage ERR_HTTP_STATUS_CODE = new ErrorMessage(ERR_CODE_HTTP_RESPONSEPARSER, "数据请求失败，请稍后重试");
  /** 服务器连接失败**/
  public final static ErrorMessage ERR_HTTP_SERVICECONNECTION = new ErrorMessage(ERR_CODE_HTTP_SERVICECONNECTION, "服务器连接失败，请稍后重试");
}
