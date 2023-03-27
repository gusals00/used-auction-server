package com.auction.usedauction.exception;

import org.apache.tomcat.util.http.fileupload.FileUploadException;

public class FileEmptyException extends RuntimeException {
    public FileEmptyException() {
        super();
    }

    public FileEmptyException(String msg) {
        super(msg);
    }

    public FileEmptyException(String msg, Throwable cause) {
        super(msg, cause);
    }


}
