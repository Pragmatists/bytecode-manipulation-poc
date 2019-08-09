package com.pragmatists.manipulation.bytecode.generation;

public enum Identifiers {
    CONSTRUCTOR_METHOD_NAME("<init>");

    private final String value;

    Identifiers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
