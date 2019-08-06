package com.pragmatists.weaving.bytecode.injection;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

public class MethodBytecodeInjector extends ClassVisitor {
    private final String methodName;
    private final List<Consumer<MethodVisitor>> operations;
    private final ClassVisitor classVisitor;

    public MethodBytecodeInjector(String methodName, List<Consumer<MethodVisitor>> operations, ClassVisitor classVisitor) {
        super(ASM7, classVisitor);
        this.methodName = methodName;
        this.operations = operations;
        this.classVisitor = classVisitor;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = classVisitor.visitMethod(access, name, descriptor, signature, exceptions);
        if (methodVisitor == null || !methodName.equals(name)) {
            return methodVisitor;
        }

        return new InjectingMethodVisitor(methodVisitor, operations);
    }

}
