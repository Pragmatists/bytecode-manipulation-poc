package com.pragmatists.weaving.utils;

public class ClassNames {
    public static String binaryToInternal(String name) {
        return name.replace('.', '/');
    }
}
