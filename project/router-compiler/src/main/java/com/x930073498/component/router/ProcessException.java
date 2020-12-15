package com.x930073498.component.router;

public class ProcessException extends RuntimeException {

    public ProcessException() {
    }

    public ProcessException(String s) {
        super(s);
    }

    public ProcessException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProcessException(Throwable throwable) {
        super(throwable);
    }

}
