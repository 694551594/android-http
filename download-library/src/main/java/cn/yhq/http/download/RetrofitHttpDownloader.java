package cn.yhq.http.download;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.ICall;
import cn.yhq.http.core.IHttpRequestListener;
import cn.yhq.http.core.IHttpResponseListener;
import cn.yhq.http.core.ProgressListener;
import cn.yhq.http.core.ProgressResponseBody;
import okhttp3.ResponseBody;

public final class RetrofitHttpDownloader implements IHttpDownloader {
    private ICall<ResponseBody> mCall;
    private final static Executor executor = Executors.newFixedThreadPool(5);

    public RetrofitHttpDownloader(ICall<ResponseBody> call) {
        this.mCall = call;
    }

    static class DownloadAsyncTask extends AsyncTask<ResponseBody, Integer, File> {
        private DownloadTask downloadTask;
        private Exception e;

        public DownloadAsyncTask(DownloadTask downloadTask) {
            this.downloadTask = downloadTask;
        }

        @Override
        protected File doInBackground(ResponseBody... params) {
            try {
                File file = downloadTask.getDownloadFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(params[0].bytes());
                fos.close();
                return file;
            } catch (Exception e) {
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            if (result == null) {
                downloadTask.getDownloadResponseListenerDispatcher().onException(downloadTask.getId(), e);
            } else {
                downloadTask.getDownloadResponseListenerDispatcher().onSuccess(downloadTask.getId(), result);
            }
        }

    }

    @Override
    public void download(Context context, final DownloadTask downloadTask) {
        // 执行下载请求
        new HttpRequester.Builder<ResponseBody>(context)
                .call(mCall)
                .listener((IHttpRequestListener) null)
                .listener(new IHttpResponseListener<ResponseBody>() {

                    @Override
                    public void onResponse(Context context, int requestCode, ResponseBody response,
                                           boolean isFromCache) {
                        ProgressResponseBody progressResponseBody = new ProgressResponseBody(response);
                        progressResponseBody.setProgressListener(new ProgressListener() {
                            @Override
                            public void onProgress(long bytesRead, long contentLength) {
                                int progress = (int) ((bytesRead * 1.0 / contentLength) * 100);
                                if (progress < 0) {
                                    progress = 0;
                                } else if (progress > 100) {
                                    progress = 100;
                                }
                                downloadTask.getDownloadProgressListenerDispatcher().onProgress(progress);
                            }
                        });
                        if (!isFromCache) {
                            new DownloadAsyncTask(downloadTask).executeOnExecutor(executor, progressResponseBody);
                        }

                    }

                    @Override
                    public void onException(Context context, Throwable t) {
                        downloadTask.getDownloadResponseListenerDispatcher().onException(downloadTask.getId(), t);
                    }

                }).request();
    }

    @Override
    public void cancel(Context context, DownloadTask downloadTask) {
        mCall.cancel();
    }

}
