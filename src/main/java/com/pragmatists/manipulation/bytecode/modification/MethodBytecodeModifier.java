package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * TODO
 */
public class MethodBytecodeModifier extends ClassVisitor {
    private final String methodName;
    private final String descriptor;
    private final Instructions instructions;
    private final ClassVisitor classVisitor;
    private final BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider;

    public MethodBytecodeModifier(String methodName,
                                  Instructions instructions,
                                  ClassVisitor classVisitor,
                                  BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        this(methodName, null, instructions, classVisitor, methodVisitorProvider);
    }

    public MethodBytecodeModifier(String methodName,
                                  String descriptor,
                                  Instructions instructions,
                                  ClassVisitor classVisitor,
                                  BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        super(Config.ASM_VERSION, classVisitor);

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;
        this.instructions = instructions;
        this.classVisitor = classVisitor;
        this.methodVisitorProvider = methodVisitorProvider;
        this.descriptor = descriptor;
    }

    public static byte[] modifyMethodInClassfile(String methodName,
                                                 String descriptor,
                                                 byte[] bytecode,
                                                 Instructions instructions,
                                                 BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodBytecodeModifier modifier =
                new MethodBytecodeModifier(methodName, descriptor, instructions, classWriter, methodVisitorProvider);

        classReader.accept(modifier, 0);
        return classWriter.toByteArray();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = classVisitor.visitMethod(access, name, descriptor, signature, exceptions);
        if (methodIsForModification(name, descriptor, methodVisitor)) { // TODO test
            return methodVisitorProvider.apply(methodVisitor, instructions);
        } else {
            return methodVisitor;
        }
    }

    private boolean methodIsForModification(String name, String descriptor, MethodVisitor methodVisitor) {
        return methodVisitor != null && methodName.equals(name) && (this.descriptor == null || this.descriptor.equals(descriptor));
    }

}
