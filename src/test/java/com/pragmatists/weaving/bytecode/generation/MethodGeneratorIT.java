package com.pragmatists.weaving.bytecode.generation;

import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static com.pragmatists.weaving.bytecode.generation.MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS;
import static com.pragmatists.weaving.type.Types.internalName;
import static com.pragmatists.weaving.type.Types.methodDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.objectweb.asm.Opcodes.*;

class MethodGeneratorIT {
    private static final String METHOD_NAME = "foo";
    private static final String FULLY_QUALIFIED_CLASS_NAME = "pkg.GeneratedClass";

    @Test
    void shouldAutoGenerateReturnForVoid()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(null);
        MethodGenerator returnThis = MethodGenerator.builder()
                .characteristic(methodCharacteristic)
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
    void shouldAutoGenerateReturnForObjects()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name(METHOD_NAME)
                .paramTypes(Collections.emptyList())
                .returnType(Object.class)
                .build();
        MethodGenerator returnThis = MethodGenerator.builder()
                .characteristic(methodCharacteristic)
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
    void shouldAutoGenerateReturnForArrays()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name(METHOD_NAME)
                .paramTypes(Collections.singletonList(int[].class))
                .returnType(int[].class)
                .build();
        MethodGenerator returnParameter = MethodGenerator.builder()
                .characteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitVarInsn(ALOAD, 1)) // push first local variable (=first argument) onto the stack
                .build();

        byte[] bytecode = getBytecode(returnParameter);

        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, int[].class);
        Object generatedClassInstance = constructor.newInstance();

        final int[] returned = {1, 5};
        Object result = method.invoke(generatedClassInstance, returned); // will pop the argument back as result

        assertEquals(returned, result);
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveBoolean()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(boolean.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(true, result); // ICONST_3 pushed 3 to the stack; 3 > 0, hence `true`
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveChar()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(char.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((char) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveByte()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(byte.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((byte) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveShort()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(short.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals((short) 3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveInt()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(int.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, ICONST_3);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(3, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveLong()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(long.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, LCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1L, result); // LCONST_1 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveFloat()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(float.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, FCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1.0f, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldAutoGenerateReturnForPrimitiveDouble()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        MethodCharacteristic methodCharacteristic = getCharacteristicWithNoParamsReturning(double.class);
        MethodGenerator returnParameter = getMethodGeneratorWithOneOpcode(methodCharacteristic, DCONST_1);

        Object result = invokeDefinedMethod(returnParameter);
        assertEquals(1.0d, result); // ICONST_3 pushed 3 to the stack
    }

    @Test
    void shouldHandleAllKindsOfParameterTypes()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MethodCharacteristic methodCharacteristic = getCharacteristic(String.class, String.class, int.class, String[].class);
        MethodGenerator build = MethodGenerator.builder()
                .characteristic(methodCharacteristic)
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
                .characteristic(methodCharacteristic)
                .methodBodyWriter(mv -> mv.visitInsn(opcode))
                .build();
    }

    private Object invokeDefinedMethod(MethodGenerator returnParameter, Object... params)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        byte[] bytecode = getBytecode(returnParameter);
        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, paramsToTypes(params));
        Object generatedClassInstance = constructor.newInstance();

        return method.invoke(generatedClassInstance, params);
    }

    private Object invokeDefinedMethodPrimitiveIntParams(MethodGenerator returnParameter, Object... params)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        byte[] bytecode = getBytecode(returnParameter);
        Class<?> generatedClass = getLoadedClass(bytecode);
        Constructor<?> constructor = getConstructor(generatedClass);
        Method method = generatedClass.getMethod(METHOD_NAME, paramsToTypes(params, c -> {
            if (c == Integer.class) {
                return int.class;
            }

            return c;
        }));
        Object generatedClassInstance = constructor.newInstance();

        return method.invoke(generatedClassInstance, params);
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
        return MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name(METHOD_NAME)
                .paramTypes(Collections.emptyList())
                .returnType(type)
                .build();
    }

    private MethodCharacteristic getCharacteristic(Class returnType, Class... params) {
        return MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name(METHOD_NAME)
                .paramTypes(Arrays.asList(params))
                .returnType(returnType)
                .build();
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