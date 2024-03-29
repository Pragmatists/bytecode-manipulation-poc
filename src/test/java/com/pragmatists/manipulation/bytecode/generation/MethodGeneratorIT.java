package com.pragmatists.manipulation.bytecode.generation;

import com.pragmatists.manipulation.bytecode.characteristics.ClassCharacteristic;
import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static com.pragmatists.manipulation.bytecode.generation.MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS;
import static com.pragmatists.manipulation.type.Types.internalName;
import static com.pragmatists.manipulation.type.Types.methodDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.objectweb.asm.Opcodes.*;

class MethodGeneratorIT {
    private static final String METHOD_NAME = "foo";
    private static final String FULLY_QUALIFIED_CLASS_NAME = "pkg.GeneratedClass";

    @Test
    void shouldAutoGenerateReturnForVoid() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(null);
        MethodGenerator returnThis = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitVarInsn(ALOAD, 0)) // return this;
                .build();

        byte[] bytecode = getBytecode(returnThis);

        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME);
        Object generatedClassInstance = constructor.newInstance();

        Object result = method.invoke(generatedClassInstance);
        assertNull(result); // should be null despite there being one object on the stack
    }

    @Test
    void shouldAutoGenerateReturnForObjects() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        MethodCharacteristic methodCharacteristic =
                new MethodCharacteristic(ACC_PUBLIC, METHOD_NAME, emptyList(), Object.class);
        MethodGenerator returnThis = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitVarInsn(ALOAD, 0)) // push `this` onto the stack
                .build();

        byte[] bytecode = getBytecode(returnThis);

        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME);
        Object generatedClassInstance = constructor.newInstance();

        Object result = method.invoke(generatedClassInstance);
        assertEquals(generatedClassInstance, result); // the method is `return this;` after all
    }

    @Test
    void shouldAutoGenerateReturnForArrays() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        MethodCharacteristic methodCharacteristic =
                new MethodCharacteristic(ACC_PUBLIC, METHOD_NAME, singletonList(int[].class), int[].class);
        MethodGenerator returnParameter = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitVarInsn(ALOAD, 1)) // push first local variable (=first argument) onto the stack
                .build();

        byte[] bytecode = getBytecode(returnParameter);

        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, int[].class);
        Object generatedClassInstance = constructor.newInstance();

        int[] returned = {1, 5};
        Object result = method.invoke(generatedClassInstance, new Object[]{returned}); // will pop the argument back as result

        assertEquals(returned, result);
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveBoolean() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(boolean.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(true, result); // ICONST_3 pushed 3 to the stack; 3 > 0, hence `true`
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveChar() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(char.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((char) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveByte() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(byte.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((byte) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveShort() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(short.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((short) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveInt() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(int.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveLong() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(long.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, LCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1L, result); // LCONST_1 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveFloat() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(float.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, FCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1.0f, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveDouble() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(double.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, DCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1.0d, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldHandleAllKindsOfParameterTypes() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MethodCharacteristic methodCharacteristic = getCharacteristic(String.class, String.class, int.class, String[].class);
        MethodGenerator build = MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic)
                .methodBodyWriter(mv -> {
                    mv.visitVarInsn(ALOAD, 1);      // pushing the String argument to stack
                    mv.visitVarInsn(ALOAD, 3);      // pushing the String[] argument to stack
                    mv.visitVarInsn(ILOAD, 2);      // pushing the int argument to stack
                    mv.visitInsn(AALOAD);           // popping two previous elements,
                    // getting the argument2-th e,lement from the argument3 array onto the stack
                    mv.visitMethodInsn(INVOKEVIRTUAL, internalName(String.class), "concat", methodDescriptor(String.class, String.class), false);
                    // concatenate the two Strings on the stack and push onto the stack
                })
                .build();

        Object result = invokeDefinedMethodPrimitiveIntParams(build, "x", 3, new String[]{"0", "1", "2", "3", "4"});
        assertEquals("x3", result);
    }

    private MethodGenerator getMethodGeneratorWithOneOpcode(MethodCharacteristic methodCharacteristic, int opcode) {
        return MethodGenerator.builder()
                .methodCharacteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitInsn(opcode))
                .build();
    }

    private Object invokeDefinedMethod(MethodGenerator returnParameter, Object... params) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        byte[] bytecode = getBytecode(returnParameter);
        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, paramsToTypes(params));
        Object generatedClassInstance = constructor.newInstance();

        return method.invoke(generatedClassInstance, params);
    }

    private Object invokeDefinedMethodPrimitiveIntParams(MethodGenerator returnParameter, Object... params) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        byte[] bytecode = getBytecode(returnParameter);
        Class<?> generatedClass = getLoadedClass(bytecode);

        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, paramsToTypes(params, this::replaceIntegerWithPrimitiveInt));
        Object generatedClassInstance = constructor.newInstance();

        return method.invoke(generatedClassInstance, params);
    }

    private Class replaceIntegerWithPrimitiveInt(Class c) {
        if (c == Integer.class) {
            return int.class;
        }

        return c;
    }

    private Class[] paramsToTypes(Object[] params) {
        return Arrays.stream(params)
                .map(Object::getClass)
                .toArray(Class[]::new);
    }

    private Class[] paramsToTypes(Object[] params, Function<Class, Class> tranformation) {
        return Arrays.stream(params)
                .map(Object::getClass)
                .map(tranformation)
                .toArray(Class[]::new);
    }

    private MethodCharacteristic getCharacteristicWithNoParamsReturning(Class type) {
        return new MethodCharacteristic(ACC_PUBLIC, METHOD_NAME, emptyList(), type);
    }

    private MethodCharacteristic getCharacteristic(Class returnType, Class... params) {
        return new MethodCharacteristic(ACC_PUBLIC, METHOD_NAME, Arrays.asList(params), returnType);
    }

    private Class<?> getLoadedClass(byte[] bytecode) throws ClassNotFoundException {
        ClassSubstitutor classSubstitutor = new ClassSubstitutor(Map.of(FULLY_QUALIFIED_CLASS_NAME, bytecode));
        return classSubstitutor.loadClass(FULLY_QUALIFIED_CLASS_NAME);
    }

    private Constructor<?> getConstructor(Class<?> aClass) {
        return aClass.getConstructors()[0];
    }

    private byte[] getBytecode(MethodGenerator returnThis) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassCharacteristic classCharacteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(FULLY_QUALIFIED_CLASS_NAME)
                .superName(Object.class.getName())
                .interfaces(new String[0])
                .build();

        return ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(Arrays.asList(DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS, returnThis))
                .build()
                .generate(classWriter);
    }
}