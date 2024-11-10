package com.learningspring.hogwartsartifactonline.system.exception;

public class CustomBlobStorageException extends RuntimeException {

        public CustomBlobStorageException(String message, Throwable cause) {
            super(message, cause);
        }
}
