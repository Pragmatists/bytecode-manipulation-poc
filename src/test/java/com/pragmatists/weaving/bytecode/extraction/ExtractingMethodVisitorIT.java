package com.pragmatists.weaving.bytecode.extraction;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.bytecode.generation.ClassBytecodeGenerator;
import com.pragmatists.weaving.bytecode.generation.ClassCharacteristic;
import com.pragmatists.weaving.bytecode.generation.MethodGenerator;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import test.control.ForTest;
import test.control.IfTest;
import test.control.InvokeStaticTest;
import test.control.TryCatchTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V11;

class ExtractingMethodVisitorIT {
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String GENERATED_CLASS_NAME = "GeneratedClass";

    @Test
    void shouldCorrectlyExtractCodeWithSimpleIf() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class testClass = IfTest.class;
        byte[] testClassBytecode = getTestClassBytecode(testClass);
        String methodName = getOnlyMethodName(testClass);
        MethodInstructionsExtractor methodExtractor = new MethodInstructionsExtractor(methodName);
        Instructions methodInstructions = methodExtractor.extract(testClassBytecode).get();

        ClassCharacteristic classCharacteristic = getClassCharacteristic(GENERATED_CLASS_NAME);
        byte[] bytecode = generateBytecode(methodInstructions, classCharacteristic);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(Map.of(GENERATED_CLASS_NAME, bytecode));

        Class<?> generatedClass = classSubstitutor.loadClass(GENERATED_CLASS_NAME);
        Object instance = getInstance(generatedClass);
        Method choose = generatedClass.getDeclaredMethod(methodName, boolean.class);

        assertEquals("left", choose.invoke(instance, false));
        assertEquals("right", choose.invoke(instance, true));
    }

    @Test
    void shouldCorrectlyExtractCodeWithSimpleFor() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class testClass = ForTest.class;
        byte[] testClassBytecode = getTestClassBytecode(testClass);
        String methodName = getOnlyMethodName(testClass);
        MethodInstructionsExtractor methodExtractor = new MethodInstructionsExtractor(methodName);
        Instructions methodInstructions = methodExtractor.extract(testClassBytecode).get();

        ClassCharacteristic classCharacteristic = getClassCharacteristic(GENERATED_CLASS_NAME);
        byte[] bytecode = generateBytecode(methodInstructions, classCharacteristic);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(Map.of(GENERATED_CLASS_NAME, bytecode));

        Class<?> generatedClass = classSubstitutor.loadClass(GENERATED_CLASS_NAME);
        Object instance = getInstance(generatedClass);
        Method loop = generatedClass.getDeclaredMethod(methodName, int.class);

        assertEquals("xxxxx", loop.invoke(instance, 5));
        assertEquals("xxxxxxxxxx", loop.invoke(instance, 10));
    }

    @Test
    void shouldCorrectlyExtractCodeInvokestatic() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class testClass = InvokeStaticTest.class;
        byte[] testClassBytecode = getTestClassBytecode(testClass);
        String methodName = getOnlyMethodName(testClass);
        MethodInstructionsExtractor methodExtractor = new MethodInstructionsExtractor(methodName);
        Instructions methodInstructions = methodExtractor.extract(testClassBytecode).get();

        ClassCharacteristic classCharacteristic = getClassCharacteristic(GENERATED_CLASS_NAME);
        byte[] bytecode = generateBytecode(methodInstructions, classCharacteristic);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(Map.of(GENERATED_CLASS_NAME, bytecode));

        Class<?> generatedClass = classSubstitutor.loadClass(GENERATED_CLASS_NAME);
        Object instance = getInstance(generatedClass);
        Method invokeStatic = generatedClass.getDeclaredMethod(methodName);

        assertEquals("10", invokeStatic.invoke(instance));
    }

    @Test
    void shouldCorrectlyExtractTryCatch() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class testClass = TryCatchTest.class;
        byte[] testClassBytecode = getTestClassBytecode(testClass);
        String methodName = getOnlyMethodName(testClass);
        MethodInstructionsExtractor methodExtractor = new MethodInstructionsExtractor(methodName);
        Instructions methodInstructions = methodExtractor.extract(testClassBytecode).get();

        ClassCharacteristic classCharacteristic = getClassCharacteristic(GENERATED_CLASS_NAME);
        byte[] bytecode = generateBytecode(methodInstructions, classCharacteristic);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(Map.of(GENERATED_CLASS_NAME, bytecode));

        Class<?> generatedClass = classSubstitutor.loadClass(GENERATED_CLASS_NAME);
        Object instance = getInstance(generatedClass);
        Method tryCatch = generatedClass.getDeclaredMethod(methodName, boolean.class);

        assertEquals(new RuntimeException().toString(), tryCatch.invoke(instance, true));
        assertEquals("not thrown", tryCatch.invoke(instance, false));
    }

    private Object getInstance(Class<?> generatedClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return generatedClass.getConstructors()[0].newInstance();
    }

    private byte[] getTestClassBytecode(Class testClass) throws IOException {
        return testClass.getResourceAsStream(testClass.getSimpleName() + CLASS_FILE_EXTENSION).readAllBytes();
    }

    private String getOnlyMethodName(Class testClass) {
        return testClass.getDeclaredMethods()[0].getName();
    }

    private ClassCharacteristic getClassCharacteristic(String className) {
        return ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(className)
                .superName(Object.class.getName())
                .interfaces(new String[0])
                .build();
    }

    private byte[] generateBytecode(Instructions methodInstructions, ClassCharacteristic classCharacteristic) {
        return ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(Arrays.asList(
                        MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS,
                        MethodGenerator.of(methodInstructions)))
                .build()
                .generate(new ClassWriter(ClassWriter.COMPUTE_FRAMES));
    }
}