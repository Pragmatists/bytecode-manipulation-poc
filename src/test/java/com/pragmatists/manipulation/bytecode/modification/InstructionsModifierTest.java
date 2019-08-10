package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class InstructionsModifierTest {
    private static final int ACCESS_FLAG = 0;
    private static final String METHOD_NAME = "testMethod";
    private static final String SOME_OTHER_METHOD_NAME = "someOtherMethod";
    private static final String DESCRIPTOR = "descriptor";
    private static final String SOME_OTHER_DESCRIPTOR = "some other descriptor";
    private static final String[] EXCEPTIONS = {};

    @Mock
    private Instructions instructions;

    @Mock
    private ClassVisitor classVisitor;

    @Mock
    private MethodVisitor methodVisitorFromClassVisitor;

    @Mock
    private ModifyingMethodVisitor injectingMethodVisitor;

    @Mock
    private ModifyingMethodVisitorProvider modifyingMethodVisitorProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(classVisitor.visitMethod(eq(ACCESS_FLAG), eq(METHOD_NAME), any(), eq(null), eq(EXCEPTIONS)))
                .thenReturn(methodVisitorFromClassVisitor);
        when(classVisitor.visitMethod(eq(ACCESS_FLAG), eq(SOME_OTHER_METHOD_NAME), any(), eq(null), eq(EXCEPTIONS)))
                .thenReturn(methodVisitorFromClassVisitor);

        when(modifyingMethodVisitorProvider.apply(methodVisitorFromClassVisitor, instructions))
                .thenReturn(injectingMethodVisitor);
    }

    @Test
    void shouldUseInjectingMethodVisitorWhenMethodNameAndDescriptorMatch() {
        InstructionsModifier instructionsModifier =
                new InstructionsModifier(METHOD_NAME, DESCRIPTOR, instructions, classVisitor, modifyingMethodVisitorProvider);

        MethodVisitor result =
                instructionsModifier.visitMethod(ACCESS_FLAG, METHOD_NAME, DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(injectingMethodVisitor, result);
    }

    @Test
    void shouldUseMethodVisitorFromClassProviderWhenMethodNameDoesNotMatch() {
        InstructionsModifier instructionsModifier =
                new InstructionsModifier(METHOD_NAME, DESCRIPTOR, instructions, classVisitor, modifyingMethodVisitorProvider);

        MethodVisitor result =
                instructionsModifier.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(methodVisitorFromClassVisitor, result);
    }

    @Test
    void shouldUseMethodVisitorFromClassProviderWhenDescriptorDoesNotMatch() {
        InstructionsModifier instructionsModifier =
                new InstructionsModifier(METHOD_NAME, DESCRIPTOR, instructions, classVisitor, modifyingMethodVisitorProvider);

        MethodVisitor result =
                instructionsModifier.visitMethod(ACCESS_FLAG, METHOD_NAME, SOME_OTHER_DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(methodVisitorFromClassVisitor, result);
    }
}