package com.pragmatists.weaving.classes;

public class ClassNameConverter {
    public String binaryToInternal(String name) {
        return name.replace('.', '/');
    }
}
