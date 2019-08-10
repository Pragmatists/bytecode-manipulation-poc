package com.pragmatists.manipulation.loaders;

import com.pragmatists.manipulation.bytecode.characteristics.ClassCharacteristic;
import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import com.pragmatists.manipulation.bytecode.generation.ClassBytecodeGenerator;
import com.pragmatists.manipulation.bytecode.generation.MethodGenerator;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import test.PrintCaptor;
import test.types.Clazz;
import test.types.Target;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.pragmatists.manipulation.bytecode.generation.MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS;
import static com.pragmatists.manipulation.type.Types.internalName;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V11;

class ClassSubstitutorTest {
    private static final String HELLO_WORLD = "Hello World!";

    @Test
    void shouldSubstituteAlreadyLoadedClass() throws IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        String className = Target.class.getName();
        Map<String, byte[]> classesToBytecode = Map.of(className, generateClassLikeTarget());

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);
        Class substitutedClass = classSubstitutor.loadClass(className);

        // The loaded class is indeed loaded by the ClassSubstitutor.
        Constructor constructor = substitutedClass.getConstructors()[0];
        assertEquals(classSubstitutor, substitutedClass.getClassLoader());

        // The behaviour of the class is as expected from the generated classes.
        Clazz target = (Clazz) constructor.newInstance();
        assertEquals("target", target.getClassId());
        assertEquals("target2", target.getClassId2());

        // The loaded class is different than the class of the same fully qualified name but loaded by the app ClassLoader.
        Target targetFromAppClassloader = new Target();
        assertNotEquals(targetFromAppClassloader.getClassId(), target.getClassId());
        assertNotEquals(targetFromAppClassloader.getClassId2(), target.getClassId2());
    }

    @Test
    void shouldValidateClassNames() {
        String invalidClassName = "1.x.y.Z";
        Map<String, byte[]> classesToBytecode = Map.of(invalidClassName, new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> new ClassSubstitutor(classesToBytecode),
                String.format("'%s' is an invalid class name.", invalidClassName));
    }

    @Test
    void shouldLoadClassNotLoadedByAnyOtherClassLoader() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        String className = "TotallyFreshClass";
        Map<String, byte[]> classesToBytecode = Map.of(className, generateSimpleClass(className));

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);
        Class<?> substitutedClass = classSubstitutor.loadClass(className);

        Object o = getInstance(substitutedClass);
        Method print = substitutedClass.getMethod("print");
        PrintCaptor printCaptor = new PrintCaptor();
        System.setOut(printCaptor);
        print.invoke(o);

        List<String> results = printCaptor.getResults();
        assertEquals(1, results.size());
        assertEquals(HELLO_WORLD, results.get(0));
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenCannotLoadBytecodeOfClassListedForSubstitution() {
        String className = "MappedToInvalidBytes";
        Map<String, byte[]> classesToBytecode = Map.of(className, new byte[]{});

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);

        assertThrows(ClassNotFoundException.class, () -> classSubstitutor.loadClass(className),
                "Could not load bytecode of class MappedToInvalidBytes listed for substitution");
    }

    private Object getInstance(Class<?> substitutedClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return substitutedClass.getConstructors()[0].newInstance();
    }

    private byte[] generateClassLikeTarget() {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassCharacteristic classCharacteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(internalName(Target.class))
                .superName(internalName(Object.class))
                .interfaces(new String[]{internalName(Clazz.class)})
                .build();

        MethodCharacteristic methodCharacteristic1 =
                new MethodCharacteristic(ACC_PUBLIC, "getClassId", Collections.emptyList(), String.class);
        MethodGenerator method1 = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic1)
                .methodBodyWriter(mv -> mv.visitLdcInsn("target")).build();

        MethodCharacteristic methodCharacteristic2 =
                new MethodCharacteristic(ACC_PUBLIC, "getClassId2", Collections.emptyList(), String.class);
        MethodGenerator method2 = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic2)
                .methodBodyWriter(mv -> mv.visitLdcInsn("target2"))
                .build();

        return ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(List.of(DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS, method1, method2))
                .build()
                .generate(classWriter);
    }

    private byte[] generateSimpleClass(String className) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassCharacteristic classCharacteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(className)
                .superName(Object.class.getName())
                .interfaces(new String[0])
                .build();

        MethodCharacteristic printMethodCharacteristic =
                new MethodCharacteristic(ACC_PUBLIC, "print", Collections.emptyList(), null);
        MethodGenerator print = MethodGenerator.builder()
                .methodCharacteristic(printMethodCharacteristic)
                .methodBodyWriter(mv -> MethodGenerator.soutPrintlnInstructions(mv, HELLO_WORLD))
                .build();

        return ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(List.of(DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS, print))
                .build()
                .generate(classWriter);
    }
}