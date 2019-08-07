package com.pragmatists.weaving.bytecode.injection;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.bytecode.extraction.MethodInstructionsExtractor;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import test.PrintCaptor;
import test.types.Modified;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MethodBytecodeInjectorTest {
    private static byte[] sourceClassBytecode;
    private static byte[] targetClassBytecode;

    @BeforeAll
    static void setUp() throws IOException {
        InputStream is1 = MethodBytecodeInjectorTest.class.getClassLoader().getResourceAsStream("Source.class");
        sourceClassBytecode = is1.readAllBytes();

        InputStream is2 = MethodBytecodeInjectorTest.class.getClassLoader().getResourceAsStream("Modified.class");
        targetClassBytecode = is2.readAllBytes();
    }

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
    void setUpForEach() {
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
        MethodBytecodeInjector methodBytecodeInjector =
                new MethodBytecodeInjector(TEST_METHOD_NAME, instructions, classVisitor, injectingMethodVisitorProvider);

        MethodVisitor result =
                methodBytecodeInjector.visitMethod(ACCESS_FLAG, TEST_METHOD_NAME, "irrelevant", null, EXCEPTIONS);

        assertEquals(injectingMethodVisitor, result);
    }

    @Test
    void shouldUseMethodVisitorFromClassProviderWhenMethodNameDoesNotMatch() {
        MethodBytecodeInjector methodBytecodeInjector =
                new MethodBytecodeInjector(TEST_METHOD_NAME, instructions, classVisitor, injectingMethodVisitorProvider);

        MethodVisitor result =
                methodBytecodeInjector.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, "irrelevant", null, EXCEPTIONS);

        assertEquals(methodVisitorFromClassVisitor, result);
    }

    @Test
    void methodByteCodeInjectorIT() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodInstructionsExtractor methodInstructionsExtractor = new MethodInstructionsExtractor("bar");

        Instructions instructions = methodInstructionsExtractor.extract(sourceClassBytecode);

        ClassReader targetReader = new ClassReader(targetClassBytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        final String methodName = "foo";
        ClassVisitor bytecodeInjector =
                new MethodBytecodeInjector(methodName, instructions, tracingVisitor, PrependingMethodVisitor::new);

        targetReader.accept(bytecodeInjector, 0);

        byte[] bytes = classWriter.toByteArray();

        String targetClassName = Modified.class.getName();
        Map<String, byte[]> classNamesToBytecode = Map.of(targetClassName, bytes);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classNamesToBytecode);
        Class<?> generatedClass = classSubstitutor.loadClass(targetClassName);

        Constructor<?> constructor = generatedClass.getConstructors()[0];
        Object o = constructor.newInstance();
        Method getClassId = generatedClass.getMethod(methodName);

        PrintCaptor resultCaptor = new PrintCaptor();
        System.setOut(resultCaptor);

        getClassId.invoke(o);

        List<String> results = resultCaptor.getResults();
        assertEquals(2, results.size());
        assertEquals("this is coming from source", results.get(0));
        assertEquals("foo here", results.get(1));
    }
}