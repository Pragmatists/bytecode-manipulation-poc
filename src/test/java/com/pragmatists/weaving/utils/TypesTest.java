package com.pragmatists.weaving.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypesTest {
    // TODO need to test other methods here too

    @Test
    void shouldReturnCorrectDescriptorForVoidMethodWithoutArguments() {
        String result = Types.methodDescriptor(null);

        assertEquals("()V", result);
    }

    @Test
    void shouldReturnCorrectDescriptorForVoidMethodWithOneArgument() {
        String result = Types.methodDescriptor(null, Object.class);

        assertEquals("(Ljava/lang/Object;)V", result);
    }

    @Test
    void shouldReturnCorrectDescriptorForVoidMethodWithMultipleArguments() {
        String result = Types.methodDescriptor(null, Object.class, String.class, Integer.class);

        assertEquals("(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Integer;)V", result);
    }

    @Test
    void shouldReturnCorretDescriptorForNonVoidMethodWithoutArguments() {
        String result = Types.methodDescriptor(Object.class);

        assertEquals("()Ljava/lang/Object;", result);
    }

    @Test
    void shouldReturnCorrectDescriptorForNonVoidMethodWithOneArgument() {
        String result = Types.methodDescriptor(Object.class, Object.class);

        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", result);
    }

    @Test
    void shouldReturnCorrectDescriptorForNonVoidMethodWithMultipleArguments() {
        String result = Types.methodDescriptor(Object.class, Object.class, String.class, Integer.class);

        assertEquals("(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Integer;)Ljava/lang/Object;", result);
    }

    @Test
    void shouldReturnCorrectDescriptorWhenArraysInvolved() {
        String result = Types.methodDescriptor(String[][].class, Object[].class);

        assertEquals("([Ljava/lang/Object;)[[Ljava/lang/String;", result);
    }

    @Test
    void shouldReturnCorrectDescriptorWhenPrimitiveTypesInvolved() {
        String result = Types.methodDescriptor(int[].class, boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class);

        assertEquals("(ZCBSIJFD)[I", result);
    }
}