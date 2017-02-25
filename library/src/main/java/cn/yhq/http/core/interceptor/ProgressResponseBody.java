package cn.yhq.http.core.interceptor;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;


class ProgressResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private Request request;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(Request request, ResponseBody responseBody, ProgressListener progressListener) {
        this.request = request;
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (responseBody.contentType() != null) {
                    progressListener.update(request,
                            responseBody.contentType().toString().contains(MultipartBody.FORM.toString()),
                            totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                } else {
                    progressListener.update(request, false, totalBytesRead, responseBody.contentLength(),
                            bytesRead == -1);
                }

                return bytesRead;
            }
        };
    }
}
