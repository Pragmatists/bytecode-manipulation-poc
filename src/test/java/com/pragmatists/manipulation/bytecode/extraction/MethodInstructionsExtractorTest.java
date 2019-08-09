package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.generation.ClassBytecodeGenerator;
import com.pragmatists.manipulation.bytecode.generation.ClassCharacteristic;
import com.pragmatists.manipulation.bytecode.generation.MethodCharacteristic;
import com.pragmatists.manipulation.bytecode.generation.MethodGenerator;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.pragmatists.manipulation.ClassFileUtils.getBytecode;
import static com.pragmatists.manipulation.bytecode.generation.MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V11;

class MethodInstructionsExtractorTest {
    private static final byte[] SOURCE_CLASS_BYTECODE = getBytecode("Target.class");

    private static final int ACCESS_FLAG = 0;
    private static final String TEST_METHOD_NAME = "testMethod";
    private static final String SOME_OTHER_METHOD_NAME = "someOtherMethod";
    private static final String DESCRIPTOR = "()V";
    private static final String SOME_OTHER_DESCRIPTOR = "()Ljava/lang/String;";
    private static final String[] EXCEPTIONS = {};
    private static final byte[] TEST_BYTES = new byte[]{0, 1, 2};
    private static final String GENERATED_CLASS_NAME = "GeneratedClass";
    private static final String GENERATED_METHOD_NAME = "foo";

    @Mock
    Function<byte[], ClassReader> classReaderProvider;

    @Mock
    ClassReader classReader;

    @Mock
    Function<MethodCharacteristic, ExtractingMethodVisitor> extractingMethodVisitorProvider;

    @Mock
    ExtractingMethodVisitor extractingMethodVisitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(classReaderProvider.apply(TEST_BYTES)).thenReturn(classReader);
        when(extractingMethodVisitorProvider.apply(getTestCharacteristic())).thenReturn(extractingMethodVisitor);
    }

    @Test
    void shouldReturnExtractingMethodVisitorWhenMethodNameAndDescriptorMatch() {
        MethodInstructionsExtractor methodInstructionsExtractor =
                new MethodInstructionsExtractor(TEST_METHOD_NAME, null, classReaderProvider, extractingMethodVisitorProvider);

        MethodVisitor result = methodInstructionsExtractor.visitMethod(ACCESS_FLAG, TEST_METHOD_NAME, DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(extractingMethodVisitor, result);
    }

    @Test
    void shouldReturnDefaultMethodVisitorWhenMethodNameDoesNotMatch() {
        MethodInstructionsExtractor methodInstructionsExtractor =
                new MethodInstructionsExtractor(TEST_METHOD_NAME, classReaderProvider, extractingMethodVisitorProvider);

        MethodVisitor result = methodInstructionsExtractor.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(methodInstructionsExtractor.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, DESCRIPTOR, null, EXCEPTIONS), result);
    }

    @Test
    void shouldReturnDefaultMethodVisitorWhenDescriptorDoesNotMatch() {
        MethodInstructionsExtractor methodInstructionsExtractor =
                new MethodInstructionsExtractor(TEST_METHOD_NAME, classReaderProvider, extractingMethodVisitorProvider);

        MethodVisitor result = methodInstructionsExtractor.visitMethod(ACCESS_FLAG, TEST_METHOD_NAME, SOME_OTHER_DESCRIPTOR, null, EXCEPTIONS);

        assertEquals(methodInstructionsExtractor.visitMethod(ACCESS_FLAG, SOME_OTHER_METHOD_NAME, SOME_OTHER_DESCRIPTOR, null, EXCEPTIONS), result);
    }

    @Test
    void shouldCollectOperationsIT() {
        MethodInstructionsExtractor methodInstructionsExtractor = new MethodInstructionsExtractor("getClassId");

        Instructions instructions = methodInstructionsExtractor.extract(SOURCE_CLASS_BYTECODE).get();

        MethodVisitor methodVisitor = mock(MethodVisitor.class);
        instructions.appendMethodInstructions(methodVisitor);

        ArgumentCaptor<Label> labelsCaptor = ArgumentCaptor.forClass(Label.class);

        verify(methodVisitor).visitLabel(labelsCaptor.capture());
        verify(methodVisitor).visitLineNumber(eq(6), labelsCaptor.capture());
        verify(methodVisitor).visitLdcInsn("target");
        verifyNoMoreInteractions(methodVisitor);

        List<Label> labels = labelsCaptor.getAllValues();
        assertEquals(labels.get(0), labels.get(1));
    }

    @Test
    void extractedInstructionsShouldWorkInWholeBytecodeIT()
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodInstructionsExtractor methodInstructionsExtractor = new MethodInstructionsExtractor("getClassId");

        Instructions instructions = methodInstructionsExtractor.extract(SOURCE_CLASS_BYTECODE).get();
        byte[] classBytecode = createClass(instructions, GENERATED_METHOD_NAME, GENERATED_CLASS_NAME);

        Class<?> generatedClass = loadClass(classBytecode);

        Constructor constructor = generatedClass.getConstructors()[0];
        Object o = constructor.newInstance();
        Method getClassId = generatedClass.getMethod(GENERATED_METHOD_NAME);
        Object invoke = getClassId.invoke(o);

        // This is value returned by test.types.Target::getClassId from the class file in the resources.
        // To avoid mistakes the source for test.types.Target::getClassId has a different value returned.
        String expected = "target";
        assertEquals(expected, invoke);
    }

    private MethodCharacteristic getTestCharacteristic() {
        return MethodCharacteristic.builder()
                .accessFlag(ACCESS_FLAG)
                .name(TEST_METHOD_NAME)
                .descriptor(DESCRIPTOR)
                .build();
    }

    private Class<?> loadClass(byte[] classBytecode) {
        Map<String, byte[]> classNamesToBytecode = Map.of(GENERATED_CLASS_NAME, classBytecode);
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classNamesToBytecode);
        return classSubstitutor.loadClass(GENERATED_CLASS_NAME);
    }

    private byte[] createClass(Instructions instructions, String methodName, String className) {
        final ClassCharacteristic characteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(className)
                .superName(Object.class.getName())
                .interfaces(new String[0])
                .build();

        MethodCharacteristic methodCharacteristic = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name(methodName)
                .paramTypes(Collections.emptyList())
                .returnType(String.class)
                .build();
        final MethodGenerator methodFromInstructions = MethodGenerator.builder()
                .characteristic(methodCharacteristic)
                .methodBodyWriter(instructions::appendMethodInstructions)
                .build();

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        return ClassBytecodeGenerator.builder()
                .characteristic(characteristic)
                .methodGenerators(Arrays.asList(DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS, methodFromInstructions))
                .build()
                .generate(classWriter);
    }
}