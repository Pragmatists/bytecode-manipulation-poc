package com.pragmatists.weaving.loaders.exceptions;

public class NoBytecodeProvidedException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "No bytecode was provided for class %s";

    public NoBytecodeProvidedException(String name) {
        super(String.format(MESSAGE_TEMPLATE, name));
    }
}
