package cn.yhq.http.core;


public class HttpRequestException extends Exception {
    private static final long serialVersionUID = 6645948056042844963L;
    private int errCode;
    private ErrorMessage mErrorMessage;

    public HttpRequestException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public HttpRequestException(ErrorMessage mErrorMessage) {
        super(mErrorMessage.msg);
        this.mErrorMessage = mErrorMessage;
        this.errCode = mErrorMessage.code;
    }

    @Override
    public String toString() {
        return "{'code':'" + errCode + "', 'msg':'" + this.getLocalizedMessage() + "'}";
    }

    public ErrorMessage getErrorMessage() {
        return mErrorMessage;
    }


}
