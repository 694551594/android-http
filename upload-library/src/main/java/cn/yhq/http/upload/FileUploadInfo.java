package cn.yhq.http.upload;

import java.io.Serializable;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class FileUploadInfo implements Serializable {
    private static final long serialVersionUID = -8906033310695310343L;
    private String fileId;
    private String fileName;
    private long fileSize;
    private String type;
    private String md5;
    private long timestamp;

    public static FileUploadInfo make(String fileId, String fileName, long fileSize, String type, String md5) {
        FileUploadInfo info = new FileUploadInfo();
        info.fileId = fileId;
        info.fileName = fileName;
        info.fileSize = fileSize;
        info.type = type;
        info.timestamp = System.currentTimeMillis();
        info.md5 = md5;
        return info;
    }

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
