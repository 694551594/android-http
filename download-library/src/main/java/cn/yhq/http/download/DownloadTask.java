package cn.yhq.http.download;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 文件下载任务，负责文件的下载调度，下载监听的事件分发等等
 *
 * @author Yanghuiqiang
 *         <p>
 *         2016-1-22
 */
public final class DownloadTask {
    private Context context;
    private String url;
    private String id;
    private File localFile;
    private Map<String, String> requestHeader = new HashMap<String, String>();
    private IHttpDownloader httpDownloader;
    private List<IDownloadResponseListener> downloadResponseListeners;
    private List<IDownloaderListener> downloaderListeners;
    private List<IDownloadProgressListener> downloadProgressListeners;

    private DownloadStatus downloadStatus = DownloadStatus.STOP;

    public enum DownloadStatus {
        PAUSE, STOP, DOWNLOADING
    }

    public DownloadStatus getStatus() {
        return this.downloadStatus;
    }

    private final IDownloadResponseListener downloadResponseListenerDispatcher =
            new IDownloadResponseListener() {

                @Override
                public void onException(String taskId, Throwable e) {
                    for (IDownloadResponseListener listener : downloadResponseListeners) {
                        listener.onException(taskId, e);
                    }
                    downloaderListenerDispatcher.onException(taskId, e);
                }

                @Override
                public void onSuccess(String taskId, File file) {
                    File newFile = new File(file.getPath().replace(".tmp", ""));
                    file.renameTo(newFile);
                    for (IDownloadResponseListener listener : downloadResponseListeners) {
                        listener.onSuccess(taskId, newFile);
                    }
                    downloaderListenerDispatcher.onSuccess(taskId, newFile);
                }

            };

    private final IDownloaderListener downloaderListenerDispatcher = new IDownloaderListener() {

        @Override
        public void onException(String taskId, Throwable e) {
            downloadStatus = DownloadStatus.STOP;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onException(taskId, e);
            }
        }

        @Override
        public void onSuccess(String taskId, File file) {
            downloadStatus = DownloadStatus.STOP;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onSuccess(taskId, file);
            }
        }

        @Override
        public void onStart(String taskId) {
            downloadStatus = DownloadStatus.DOWNLOADING;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onStart(taskId);
            }
        }

        @Override
        public void onCancel(String taskId) {
            downloadStatus = DownloadStatus.STOP;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onCancel(taskId);
            }
        }

        @Override
        public void onPause(String taskId) {
            downloadStatus = DownloadStatus.PAUSE;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onPause(taskId);
            }
        }

        @Override
        public void onResume(String taskId) {
            downloadStatus = DownloadStatus.DOWNLOADING;
            for (IDownloaderListener listener : downloaderListeners) {
                listener.onResume(taskId);
            }
        }

    };

    private final IDownloadProgressListener downloadProgressListenerDispatcher =
            new IDownloadProgressListener() {

                @Override
                public void onProgress(int progress) {
                    for (IDownloadProgressListener listener : downloadProgressListeners) {
                        listener.onProgress(progress);
                    }
                }

            };

    public static class Builder {
        private Context context;
        private String url;
        private File localFile;
        private IHttpDownloader httpDownloader;
        private Map<String, String> requestHeader = new HashMap<String, String>();
        private List<IDownloadResponseListener> downloadResponseListeners;
        private List<IDownloadProgressListener> downloadProgressListeners;
        private List<IDownloaderListener> downloaderListeners;

        public DownloadTask build() {
            DownloadTask downloadTask = new DownloadTask(this);
            return downloadTask;
        }

        public Builder(Context context) {
            this.context = context;
            this.downloaderListeners = new CopyOnWriteArrayList<>();
            this.downloadProgressListeners = new CopyOnWriteArrayList<>();
            this.downloadResponseListeners = new CopyOnWriteArrayList<>();
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder path(String localPath) {
            this.localFile = new File(localPath);
            return this;
        }

        public Builder file(File localFile) {
            this.localFile = localFile;
            return this;
        }

        public Builder downloader(IHttpDownloader httpDownloader) {
            this.httpDownloader = httpDownloader;
            return this;
        }

        public Builder header(String key, String value) {
            if (requestHeader == null) {
                this.requestHeader = new HashMap<>();
            }
            this.requestHeader.put(key, value);
            return this;
        }

        public Builder header(Map<String, String> requestHeader) {
            this.requestHeader = requestHeader;
            return this;
        }

        public Builder response(List<IDownloadResponseListener> downloadResponseListeners) {
            this.downloadResponseListeners.addAll(downloadResponseListeners);
            return this;
        }

        public Builder progress(List<IDownloadProgressListener> downloadProgressListeners) {
            this.downloadProgressListeners.addAll(downloadProgressListeners);
            return this;
        }

        public Builder listener(List<IDownloaderListener> downloaderListeners) {
            this.downloaderListeners.addAll(downloaderListeners);
            return this;
        }

    }

    DownloadTask(Builder builder) {
        this.context = builder.context;
        this.httpDownloader = builder.httpDownloader;
        this.url = builder.url;
        this.id = UUID.randomUUID().toString();
        this.localFile = builder.localFile;
        this.requestHeader = builder.requestHeader;
        this.downloaderListeners = builder.downloaderListeners;
        this.downloadProgressListeners = builder.downloadProgressListeners;
        this.downloadResponseListeners = builder.downloadResponseListeners;
    }

    public void header(String key, String value) {
        this.requestHeader.put(key, value);
    }

    public String getId() {
        return this.id;
    }

    String getUrl() {
        return url;
    }

    File getDownloadFile() {
        return new File(localFile.getPath() + ".tmp");
    }

    Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    IDownloadResponseListener getDownloadResponseListenerDispatcher() {
        return downloadResponseListenerDispatcher;
    }

    IDownloadProgressListener getDownloadProgressListenerDispatcher() {
        return downloadProgressListenerDispatcher;
    }

    void start() {
        if (downloaderListenerDispatcher != null) {
            downloaderListenerDispatcher.onStart(id);
        }
        httpDownloader.download(context, this);
    }

    void pause() {
        // TODO 暂时不支持
        if (downloaderListenerDispatcher != null) {
            downloaderListenerDispatcher.onPause(id);
        }
    }

    void cancel() {
        if (downloaderListenerDispatcher != null) {
            downloaderListenerDispatcher.onCancel(id);
        }
        httpDownloader.cancel(context, this);
    }

    void resume() {
        // TODO 暂时不支持
        if (downloaderListenerDispatcher != null) {
            downloaderListenerDispatcher.onResume(id);
        }
    }

    Context getContext() {
        return context;
    }
}
