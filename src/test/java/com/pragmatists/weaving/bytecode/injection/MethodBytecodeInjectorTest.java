package com.pragmatists.weaving.bytecode.injection;

import com.pragmatists.weaving.bytecode.extraction.MethodBytecodeExtractor;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import testtypes.Modified;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void appenderIntegrationTest() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodBytecodeExtractor methodBytecodeExtractor = new MethodBytecodeExtractor("bar");

        ClassReader sourceReader = new ClassReader(sourceClassBytecode);
        sourceReader.accept(methodBytecodeExtractor, 0);

        List<Consumer<MethodVisitor>> operations = methodBytecodeExtractor.getExtractingMethodVisitor().getOperations();

        ClassReader targetReader = new ClassReader(targetClassBytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        ClassVisitor bytecodeInjector = new MethodBytecodeInjector("foo", operations, tracingVisitor);

        targetReader.accept(bytecodeInjector, 0);

        byte[] bytes = classWriter.toByteArray();

        String targetClassName = Modified.class.getName();
        Map<String, byte[]> classNamesToBytecode = Map.of(targetClassName, bytes);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classNamesToBytecode);
        Class<?> generatedClass = classSubstitutor.loadClass(targetClassName);

        Constructor<?> constructor = generatedClass.getConstructors()[0];
        Object o = constructor.newInstance();
        Method getClassId = generatedClass.getMethod("foo");

        PrintCaptor resultCaptor = new PrintCaptor();

        System.setOut(resultCaptor);

        getClassId.invoke(o);

        List<String> results = resultCaptor.getResults();
        assertEquals(2, results.size());
        assertEquals("this is coming from source", results.get(0));
        assertEquals("foo here", results.get(1));
    }

    class PrintCaptor extends PrintStream {
        private List<String> results = new ArrayList<>();

        public PrintCaptor() {
            super(new OutputStream() {
                @Override
                public void write(int b) { }
            });
        }

        @Override
        public void print(String x) {
            results.add(x);
        }

        public List<String> getResults() {
            return results;
        }
    }
}