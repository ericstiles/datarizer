package com.test;

public class DataRizerException extends RuntimeException {

  DataRizerException(String message) {
    super(message);
  }

  DataRizerException(Throwable cause) {
    super(cause);
  }
}
