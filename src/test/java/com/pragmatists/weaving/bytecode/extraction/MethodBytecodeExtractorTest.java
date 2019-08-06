package com.pragmatists.weaving.bytecode.extraction;

import com.pragmatists.weaving.bytecode.ConstructorGenerator;
import com.pragmatists.weaving.classes.ClassNameConverter;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.objectweb.asm.Opcodes.*;

class MethodBytecodeExtractorTest {
    private static byte[] sourceClassBytecode;
    private static byte[] targetClassBytecode;

    @BeforeAll
    static void setUp() throws IOException {
        InputStream is1 = MethodBytecodeExtractorTest.class.getClassLoader().getResourceAsStream("Target.class");
        sourceClassBytecode = is1.readAllBytes();

        InputStream is2 = MethodBytecodeExtractorTest.class.getClassLoader().getResourceAsStream("Modified.class");
        targetClassBytecode = is2.readAllBytes();
    }

    @Test
    void shouldCollectOperations() {
        MethodBytecodeExtractor methodBytecodeExtractor = new MethodBytecodeExtractor("getClassId");

        ClassReader classReader = new ClassReader(sourceClassBytecode);
        classReader.accept(methodBytecodeExtractor, 0);

        List<Consumer<MethodVisitor>> operations = methodBytecodeExtractor.getExtractingMethodVisitor().getOperations();
        assertFalse(operations.isEmpty());
        // todo better assertion :)
    }

    @Test
    void integrationTest() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodBytecodeExtractor methodBytecodeExtractor = new MethodBytecodeExtractor("getClassId");

        ClassReader classReader = new ClassReader(sourceClassBytecode);
        classReader.accept(methodBytecodeExtractor, 0);

        List<Consumer<MethodVisitor>> operations = methodBytecodeExtractor.getExtractingMethodVisitor().getOperations();

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, printWriter);
        CheckClassAdapter classVisitor = new CheckClassAdapter(tracingVisitor, false);
        ClassNameConverter nameConverter = new ClassNameConverter();
        ConstructorGenerator constructorGenerator = new ConstructorGenerator();

        classVisitor.visit(V11, ACC_PUBLIC, nameConverter.binaryToInternal("GeneratedClass"), null, nameConverter.binaryToInternal(Object.class.getName()), new String [] {});
        MethodVisitor constructorVisitor = classVisitor.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        constructorVisitor.visitCode();
        constructorGenerator.defaultConstructor(constructorVisitor);

        MethodVisitor methodVisitor = classVisitor.visitMethod(ACC_PUBLIC, "getClassId", "()Ljava/lang/String;", null, null);
        methodVisitor.visitCode();
        operations.forEach(a -> a.accept(methodVisitor));
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        classVisitor.visitEnd();

        byte[] bytes = classWriter.toByteArray();

        Map<String, byte[]> classNamesToBytecode = Map.of("GeneratedClass", bytes);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classNamesToBytecode);
        Class<?> generatedClass = classSubstitutor.loadClass("GeneratedClass");

        Constructor<?> constructor = generatedClass.getConstructors()[0];
        Object o = constructor.newInstance();
        Method getClassId = generatedClass.getMethod("getClassId");
        Object invoke = getClassId.invoke(o);

        assertEquals("target", invoke);
    }
}