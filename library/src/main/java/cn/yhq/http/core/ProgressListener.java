package cn.yhq.http.core;

public interface ProgressListener {
  void update(boolean multipart, long bytesRead, long contentLength, boolean done);
}
