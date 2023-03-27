package com.auction.usedauction.exception;

public class S3FileNotFoundException extends RuntimeException{
    public S3FileNotFoundException() {
        super();
    }

    public S3FileNotFoundException(String message) {
        super(message);
    }

    public S3FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3FileNotFoundException(Throwable cause) {
        super(cause);
    }
}
