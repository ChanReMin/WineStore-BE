package com.example.demo.exceptions;

public class FileUploadException extends RuntimeException {
  public FileUploadException(String message) {
    super(message);
  }

  public FileUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
