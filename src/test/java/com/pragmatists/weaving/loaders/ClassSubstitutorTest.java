package com.pragmatists.weaving.loaders;

import com.pragmatists.weaving.bytecode.generation.ClassBytecodeGenerator;
import com.pragmatists.weaving.bytecode.generation.ClassCharacteristic;
import com.pragmatists.weaving.bytecode.generation.MethodCharacteristic;
import com.pragmatists.weaving.bytecode.generation.MethodGenerator;
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

import static com.pragmatists.weaving.bytecode.generation.MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS;
import static com.pragmatists.weaving.utils.ClassNames.binaryToInternal;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V11;

class ClassSubstitutorTest {
    private static final String HELLO_WORLD = "Hello World!";

    @Test
    void shouldSubstituteAlreadyLoadedClass()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
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
        final String invalidClassName = "1.x.y.Z";
        Map<String, byte[]> classesToBytecode = Map.of(invalidClassName, new byte[]{});
        assertThrows(IllegalArgumentException.class, () -> new ClassSubstitutor(classesToBytecode),
                String.format("'%s' is an invalid class name.", invalidClassName));
    }

    @Test
    void shouldLoadClassNotLoadedByAnyOtherClassLoader() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        String className = "TotallyFreshClass";
        Map<String, byte[]> classesToBytecode = Map.of(className, generateSimpleClass(className));

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);
        Class<?> substitutedClass = classSubstitutor.loadClass(className);

        Object o = substitutedClass.getConstructors()[0].newInstance();
        Method print = substitutedClass.getMethod("print");

        PrintCaptor printCaptor = new PrintCaptor();
        System.setOut(printCaptor);
        print.invoke(o);

        List<String> results = printCaptor.getResults();
        assertEquals(1, results.size());
        assertEquals(HELLO_WORLD, results.get(0));
    }

    private byte[] generateClassLikeTarget() {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassCharacteristic classCharacteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(binaryToInternal(Target.class.getName()))
                .superName(binaryToInternal(Object.class.getName()))
                .interfaces(new String[]{binaryToInternal(Clazz.class.getName())})
                .build();

        MethodCharacteristic methodCharacteristic1 = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name("getClassId")
                .paramTypes(Collections.emptyList())
                .returnType(String.class)
                .build();
        MethodGenerator method1 = MethodGenerator.builder()
                .characteristic(methodCharacteristic1)
                .methodBody(mv -> mv.visitLdcInsn("target")).build();

        MethodCharacteristic methodCharacteristic2 = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name("getClassId2")
                .paramTypes(Collections.emptyList())
                .returnType(String.class)
                .build();
        MethodGenerator method2 = MethodGenerator.builder()
                .characteristic(methodCharacteristic2)
                .methodBody(mv -> mv.visitLdcInsn("target2"))
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
                .name(binaryToInternal(className))
                .superName(binaryToInternal(Object.class.getName()))
                .interfaces(new String[]{})
                .build();

        MethodCharacteristic printMethodCharacteristic = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name("print")
                .paramTypes(Collections.emptyList())
                .returnType(null)
                .build();
        MethodGenerator print = MethodGenerator.builder()
                .characteristic(printMethodCharacteristic)
                .methodBody(mv -> MethodGenerator.soutBytecode(mv, HELLO_WORLD))
                .build();

        return ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(List.of(DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS, print))
                .build()
                .generate(classWriter);
    }
}