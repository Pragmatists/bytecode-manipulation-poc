package com.pragmatists.weaving.examples;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.bytecode.modification.MethodBytecodeModifier;
import com.pragmatists.weaving.loaders.ClassSubstitutor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

public class Utils {
    private static final String CLASS_FILE_EXTENSION = ".class";

    public static Class<?> loadModifiedClass(String methodName,
                                             String descriptor,
                                             Instructions instructions,
                                             byte[] originalBytecode,
                                             BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider)
            throws ClassNotFoundException {
        var modifiedBytecode = modifyBytecode(methodName, descriptor, originalBytecode, instructions, methodVisitorProvider);

        var classSubstitutor = new ClassSubstitutor(Map.of(Calculator.class.getName(), modifiedBytecode));
        return classSubstitutor.loadClass(Calculator.class.getName());
    }

    public static byte[] getBytecode(Class c) {
        try {
            return Calculator.class.getResourceAsStream(c.getSimpleName() + CLASS_FILE_EXTENSION).readAllBytes();
        } catch (IOException e) {
            throw new FailedExampleException(e);
        }
    }

    public static byte[] modifyBytecode(String methodName,
                                        String descriptor,
                                        byte[] calculatorBytecode,
                                        Instructions instructions,
                                        BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        var classReader = new ClassReader(calculatorBytecode);
        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        var modifier = new MethodBytecodeModifier(methodName, descriptor, instructions, classWriter, methodVisitorProvider);
        classReader.accept(modifier, 0);
        return classWriter.toByteArray();
    }
}
