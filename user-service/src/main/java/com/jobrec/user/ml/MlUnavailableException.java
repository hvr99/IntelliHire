package com.jobrec.user.ml;
public class MlUnavailableException extends RuntimeException {
    public MlUnavailableException(String message, Throwable cause) { super(message, cause); }
    public MlUnavailableException(String message) { super(message); }
}
