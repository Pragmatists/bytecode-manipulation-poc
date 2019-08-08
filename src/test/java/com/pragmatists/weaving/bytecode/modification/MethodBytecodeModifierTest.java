package com.pragmatists.weaving.bytecode.modification;

import com.pragmatists.weaving.bytecode.Instructions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MethodBytecodeModifierTest {
    private static final int ACCESS_FLAG = 0;
    private static final String TEST_METHOD_NAME = "testMethod";
    private static final String SOME_OTHER_METHOD_NAME = "someOtherMethod";
    private static final String[] EXCEPTIONS = {};

    @Mock
    private Instructions instructions;

    @Mock
    private ClassVisitor classVisitor;

    @Mock
    private MethodVisitor methodVisitorFromClassVisitor;

    @Mock
    private MethodVisitor injectingMethodVisitor;

    @Mock
    private BiFunction<MethodVisitor, Instructions, MethodVisitor> injectingMethodVisitorProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(classVisitor.visitMethod(eq(ACCESS_FLAG), eq(TEST_METHOD_NAME), any(), eq(null), eq(EXCEPTIONS)))
                .thenReturn(methodVisitorFromClassVisitor);

        when(classVisitor.visitMethod(eq(ACCESS_FLAG), eq(SOME_OTHER_METHOD_NAME), any(), eq(null), eq(EXCEPTIONS)))
                .thenReturn(methodVisitorFromClassVisitor);

        when(injectingMethodVisitorProvider.apply(methodVisitorFromClassVisitor, instructions))
                .thenReturn(injectingMethodVisitor);
    }

    @Test
    void shouldUseInjectingMethodVisitorWhenMethodNameMatches() {
        MethodBytecodeModifier methodBytecodeModifier =
                new MethodBytecodeModifier(TEST_METHOD_NAME, instructions, classVisitor, injectingMethodVisitorProvider);

        MethodVisitor result =
                methodBytecodeModifier.visitMethod(ACCESS_FLAG, TEST_METHOD_NAME, "irrelevant", null, EXCEPTIONS);

        assertEquals(injectingMethodVisitor, result);
    }

    @Test
    void shouldUseMethodVisitorFromClassProviderWhenMethodNameDoesNotMatch() {
        MethodBytecodeModifier methodBytecodeModifier =
                new MethodBytecodeModifier(TEST_METHOD_NAME, instructions, classVisitor, injectingMethodVisitorProvider);

        MethodVisitor result =
                methodBytecodeModifier.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, "irrelevant", null, EXCEPTIONS);

        assertEquals(methodVisitorFromClassVisitor, result);
    }
}