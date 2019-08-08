package com.pragmatists.weaving.bytecode.modification;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.bytecode.extraction.MethodInstructionsExtractor;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

class InjectionMethodVisitorsIT {
    private static final String SOURCE_METHOD_NAME = "bar";
    private static final String MODIFIED_METHOD_NAME = "foo";
    private static byte[] sourceClassBytecode;
    private static byte[] targetClassBytecode;

    @BeforeAll
    static void setUp() throws IOException {
        InputStream is1 = MethodBytecodeModifierTest.class.getClassLoader().getResourceAsStream("Source.class");
        sourceClassBytecode = is1.readAllBytes();

        InputStream is2 = MethodBytecodeModifierTest.class.getClassLoader().getResourceAsStream("Modified.class");
        targetClassBytecode = is2.readAllBytes();
    }

    @Test
    void bytecodeShouldBeCorrectlyAppended()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Instructions instructionsFromBarMethod = extractInstructionsFromSourceMethod();

        BiFunction<MethodVisitor, Instructions, MethodVisitor> appenderProvider = AppendingMethodVisitor::new;

        byte[] bytecode = modifyMethodInModifiedClass(instructionsFromBarMethod, appenderProvider);
        Class<?> modifiedClass = loadModifiedClass(bytecode);
        List<String> results = captureSoutPrintlnResults(modifiedClass);

        assertEquals(2, results.size());
        assertEquals("foo here", results.get(0));
        assertEquals("this is coming from source", results.get(1));
    }

    @Test
    void bytecodeShouldBeCorrectlyPrepended()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Instructions instructionsFromBarMethod = extractInstructionsFromSourceMethod();

        BiFunction<MethodVisitor, Instructions, MethodVisitor> prependerProvider = PrependingMethodVisitor::new;

        byte[] bytecode = modifyMethodInModifiedClass(instructionsFromBarMethod, prependerProvider);
        Class<?> modifiedClass = loadModifiedClass(bytecode);
        List<String> results = captureSoutPrintlnResults(modifiedClass);

        assertEquals(2, results.size());
        assertEquals("this is coming from source", results.get(0));
        assertEquals("foo here", results.get(1));
    }

    private List<String> captureSoutPrintlnResults(Class<?> modifiedClass)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor constructor = getConstructor(modifiedClass);
        Object o = constructor.newInstance();
        Method fooMethod = modifiedClass.getMethod(MODIFIED_METHOD_NAME);

        PrintCaptor resultCaptor = new PrintCaptor();
        System.setOut(resultCaptor);
        fooMethod.invoke(o);

        return resultCaptor.getResults();
    }

    private Class<?> loadModifiedClass(byte[] bytes) throws ClassNotFoundException {
        String targetClassName = Modified.class.getName();
        Map<String, byte[]> classNamesToBytecode = Map.of(targetClassName, bytes);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classNamesToBytecode);
        return classSubstitutor.loadClass(targetClassName);
    }

    private Constructor<?> getConstructor(Class<?> modifiedClass) {
        return modifiedClass.getConstructors()[0];
    }

    private byte[] modifyMethodInModifiedClass(Instructions instructionsFromBarMethod, BiFunction<MethodVisitor, Instructions, MethodVisitor> aNew) {
        ClassReader targetReader = new ClassReader(targetClassBytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));

        ClassVisitor bytecodeInjector =
                new MethodBytecodeModifier(MODIFIED_METHOD_NAME, instructionsFromBarMethod, tracingVisitor, aNew);
        targetReader.accept(bytecodeInjector, 0);
        return classWriter.toByteArray();
    }

    private Instructions extractInstructionsFromSourceMethod() {
        MethodInstructionsExtractor methodInstructionsExtractor = new MethodInstructionsExtractor(SOURCE_METHOD_NAME);
        return methodInstructionsExtractor.extract(sourceClassBytecode).get();
    }

}