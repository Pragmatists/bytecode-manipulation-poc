package com.pragmatists.weaving.examples;

class FailedExampleException extends RuntimeException {
    public FailedExampleException() {
        super();
    }

    public FailedExampleException(Throwable cause) {
        super(cause);
    }
}
