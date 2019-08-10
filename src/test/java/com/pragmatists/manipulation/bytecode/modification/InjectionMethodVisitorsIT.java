package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.extraction.InstructionsExtractor;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import test.PrintCaptor;
import test.types.Modified;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.ClassFileUtils.getBytecodeFromResources;

class InjectionMethodVisitorsIT {
    private static final String SOURCE_METHOD_NAME = "bar";
    private static final String MODIFIED_METHOD_NAME = "foo";
    private static final byte[] SOURCE_CLASS_BYTECODE = getBytecodeFromResources("Source.class");
    private static final byte[] TARGET_CLASS_BYTECODE = getBytecodeFromResources("Modified.class");

    @Test
    void bytecodeShouldBeCorrectlyAppended() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Instructions instructionsFromBarMethod = extractInstructionsFromSourceMethod();

        ModifyingMethodVisitorProvider appenderProvider = AppendingMethodVisitor::new;

        byte[] bytecode = modifyMethodInModifiedClass(instructionsFromBarMethod, appenderProvider);
        Class<?> modifiedClass = loadModifiedClass(bytecode);
        List<String> results = captureSoutPrintlnResults(modifiedClass);

        assertEquals(2, results.size());
        assertEquals("foo here", results.get(0));
        assertEquals("this is coming from source", results.get(1));
    }

    @Test
    void bytecodeShouldBeCorrectlyPrepended() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Instructions instructionsFromBarMethod = extractInstructionsFromSourceMethod();

        ModifyingMethodVisitorProvider prependerProvider = PrependingMethodVisitor::new;

        byte[] bytecode = modifyMethodInModifiedClass(instructionsFromBarMethod, prependerProvider);
        Class<?> modifiedClass = loadModifiedClass(bytecode);
        List<String> results = captureSoutPrintlnResults(modifiedClass);

        assertEquals(2, results.size());
        assertEquals("this is coming from source", results.get(0));
        assertEquals("foo here", results.get(1));
    }

    private List<String> captureSoutPrintlnResults(Class<?> modifiedClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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

    private byte[] modifyMethodInModifiedClass(Instructions instructionsFromBarMethod, ModifyingMethodVisitorProvider methodModifier) {
        ClassReader targetReader = new ClassReader(TARGET_CLASS_BYTECODE);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));

        ClassVisitor bytecodeInjector =
                new InstructionsModifier(MODIFIED_METHOD_NAME, instructionsFromBarMethod, tracingVisitor, methodModifier);
        targetReader.accept(bytecodeInjector, 0);
        return classWriter.toByteArray();
    }

    private Instructions extractInstructionsFromSourceMethod() {
        InstructionsExtractor instructionsExtractor = new InstructionsExtractor(SOURCE_METHOD_NAME);
        return instructionsExtractor.extract(SOURCE_CLASS_BYTECODE).get();
    }

}