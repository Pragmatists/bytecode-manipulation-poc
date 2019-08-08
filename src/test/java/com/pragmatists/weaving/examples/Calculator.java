package com.pragmatists.weaving.examples;

public class Calculator {
    public String add(long x, long y) {
        return String.valueOf(x + y);
    }

    public String add(int x) {
        return String.valueOf(x + 1);
    }
}
