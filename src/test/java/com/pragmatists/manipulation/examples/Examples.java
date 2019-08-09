package com.pragmatists.manipulation.examples;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.extraction.MethodInstructionsExtractor;
import com.pragmatists.manipulation.bytecode.generation.ClassBytecodeGenerator;
import com.pragmatists.manipulation.bytecode.generation.ClassCharacteristic;
import com.pragmatists.manipulation.bytecode.generation.MethodCharacteristic;
import com.pragmatists.manipulation.bytecode.generation.MethodGenerator;
import com.pragmatists.manipulation.bytecode.modification.AppendingMethodVisitor;
import com.pragmatists.manipulation.bytecode.modification.MethodBytecodeModifier;
import com.pragmatists.manipulation.bytecode.modification.PrependingMethodVisitor;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;
import com.pragmatists.manipulation.type.Types;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.pragmatists.manipulation.ClassFileUtils.getBytecode;
import static com.pragmatists.manipulation.type.Types.internalName;
import static com.pragmatists.manipulation.type.Types.methodDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.objectweb.asm.Opcodes.*;

public class Examples {
    private static final Calculator ORIGINAL_CALCULATOR_INSTANCE = new Calculator();
    private static final byte[] ORIGINAL_CALCULATOR_BYTECODE = getBytecode(Calculator.class);
    private static final String ADD_METHOD_NAME = "add";
    private static final String METHOD_DESCRIPTOR = Types.methodDescriptor(String.class, long.class, long.class);
    private static final String GENERATED_CLASS_FQNAME = "pkg.GeneratedClass";

    @Test
    void simpleParameterHijacking() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        var instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitLdcInsn(-1L);           // pushing -1L onto the stack
            mv.visitVarInsn(LSTORE, 1);     // storing into first argument's slots
            mv.visitLdcInsn(0L);            // pushing 0l onto the stack
            mv.visitVarInsn(LSTORE, 3);     // storing into second argument's slots (longs take two slots)
        });

        var modifiedCalculatorClass =
                loadModifiedClass(ADD_METHOD_NAME, METHOD_DESCRIPTOR, instructions, Calculator.class, ORIGINAL_CALCULATOR_BYTECODE, PrependingMethodVisitor::new);
        var modifiedCalculator = getInstance(modifiedCalculatorClass);
        var add = modifiedCalculatorClass.getDeclaredMethod(ADD_METHOD_NAME, long.class, long.class);

        var invocationResult = (String) add.invoke(modifiedCalculator, 3L, 4L);

        assertNotEquals(ORIGINAL_CALCULATOR_INSTANCE.add(3L, 4L), invocationResult);
        assertEquals("-1", invocationResult);
    }

    @Test
    void parameterHijackingWithBytecodeExtraction() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var timerClassBytecode = getBytecode(DoubleReturner.class);
        var timerMethodExtractor = new MethodInstructionsExtractor("getValue"); // skipping descriptor, method unique

        var getTimeInstructions = timerMethodExtractor.extract(timerClassBytecode)
                .orElseThrow(FailedExampleException::new);
        var instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            getTimeInstructions.appendMethodInstructions(mv);  // adding instructions taken from DoubleReturner#returnValue()
            mv.visitMethodInsn(INVOKEVIRTUAL, internalName(Double.class), "intValue", Types.methodDescriptor(int.class), false);
            mv.visitInsn(I2L);                                 // Double#intValue() returned an int, converting it to long
            mv.visitVarInsn(LSTORE, 1);                        // popping stack into first argument's slot
        });

        var doubleValue = new DoubleReturner().getValue().intValue();
        var modifiedCalculatorClass =
                loadModifiedClass(ADD_METHOD_NAME, METHOD_DESCRIPTOR, instructions, Calculator.class, ORIGINAL_CALCULATOR_BYTECODE, PrependingMethodVisitor::new);
        var modifiedCalculator = getInstance(modifiedCalculatorClass);
        var add = modifiedCalculatorClass.getDeclaredMethod(ADD_METHOD_NAME, long.class, long.class);

        var invocationResult = (String) add.invoke(modifiedCalculator, 3L, 4L);

        assertNotEquals(ORIGINAL_CALCULATOR_INSTANCE.add(3L, 4L), invocationResult);
        assertEquals(doubleValue + 4L, Long.parseLong(invocationResult));
    }

    @Test
    void modifyingReturnedValues() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        var listReturnerClassBytecode = getBytecode(ListReturner.class);

        var injectedString = "Where did this come from?!";
        var instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitInsn(DUP);                  // ListReturner#getList() would return a list, let's duplicate this list
            mv.visitInsn(ICONST_0);             // pushing index of the first element onto the stack
            mv.visitLdcInsn(injectedString);    // pushing the String to onto the stack
            mv.visitMethodInsn(INVOKEINTERFACE, internalName(List.class), "add", Types.methodDescriptor(null, int.class, Object.class), true);
            // after three elements got popped from the stack when invoked List#add(...),
            // we're left with the original pointer to the list
        });

        var modifiedListReturnerClass =
                loadModifiedClass("getList", Types.methodDescriptor(List.class, String.class), instructions, ListReturner.class, listReturnerClassBytecode, AppendingMethodVisitor::new);
        var modifiedListReturner = getInstance(modifiedListReturnerClass);
        var getList = modifiedListReturnerClass.getDeclaredMethod("getList", String.class);

        var argument = "to be expected";
        @SuppressWarnings("unchecked")
        var result = (List<String>) getList.invoke(modifiedListReturner, argument);

        assertEquals(2, result.size());
        assertEquals(injectedString, result.get(0));
        assertEquals(argument, result.get(1));
    }

    @Test
    void generateClassImplementingAnInterfaceFromTheGround() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        var classCharacteristic = ClassCharacteristic.builder()
                .javaVersion(V11)
                .accessFlag(ACC_PUBLIC)
                .name(GENERATED_CLASS_FQNAME)
                .superName(Object.class.getName())
                .interfaces(new String[]{internalName(AnInterface.class)})
                .build();

        var instructions = new Instructions();
        var getValueMethodCharacteristic = MethodCharacteristic.builder()
                .accessFlag(ACC_PUBLIC)
                .name("getValue")
                .returnType(String.class)
                .paramTypes(Collections.singletonList(int.class))
                .build();
        instructions.setMethodCharacteristic(getValueMethodCharacteristic);
        instructions.collectInstruction(mv -> {
            mv.visitVarInsn(ILOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, internalName(String.class), "valueOf", methodDescriptor(String.class, int.class), false);
            mv.visitLdcInsn(" is a number");
            mv.visitMethodInsn(INVOKEVIRTUAL, internalName(String.class), "concat", methodDescriptor(String.class, String.class), false);
        });

        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        var bytecode = ClassBytecodeGenerator.builder()
                .characteristic(classCharacteristic)
                .methodGenerators(Arrays.asList(
                        MethodGenerator.DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS,
                        MethodGenerator.of(instructions)
                ))
                .build()
                .generate(classWriter);

        var generatedClass = loadClassViaAppClassLoader(bytecode);
        var instance = (AnInterface) getInstance(generatedClass);

        System.out.println(instance.getValue(15));
    }

    private Class loadClassViaAppClassLoader(byte[] bytecode) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method defineClassMethod =
                ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClassMethod.setAccessible(true);
        return (Class) defineClassMethod.invoke(getClass().getClassLoader(), GENERATED_CLASS_FQNAME, bytecode, 0, bytecode.length);
    }

    private Object getInstance(Class c) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return c.getConstructors()[0].newInstance();
    }

    private Class<?> loadModifiedClass(String methodName,
                                       String descriptor,
                                       Instructions instructions,
                                       Class c,
                                       byte[] originalBytecode,
                                       BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        var modifiedBytecode =
                MethodBytecodeModifier.modifyMethodInClassfile(methodName, descriptor, originalBytecode, instructions, methodVisitorProvider);
        var classSubstitutor = new ClassSubstitutor(Map.of(c.getName(), modifiedBytecode));
        return classSubstitutor.loadClass(c.getName());
    }
}
