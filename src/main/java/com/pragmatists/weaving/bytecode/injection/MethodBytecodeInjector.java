package com.pragmatists.weaving.bytecode.injection;

import com.pragmatists.weaving.bytecode.Instructions;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.function.BiFunction;

import static com.pragmatists.weaving.config.Config.ASM_VERSION;

public class MethodBytecodeInjector extends ClassVisitor {
    private final String methodName;
    private final Instructions instructions;
    private final ClassVisitor classVisitor;
    private final BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider;

    public MethodBytecodeInjector(String methodName,
                                  Instructions instructions,
                                  ClassVisitor classVisitor,
                                  BiFunction<MethodVisitor, Instructions, MethodVisitor> methodVisitorProvider) {
        super(ASM_VERSION, classVisitor);

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;
        this.instructions = instructions;
        this.classVisitor = classVisitor;
        this.methodVisitorProvider = methodVisitorProvider;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = classVisitor.visitMethod(access, name, descriptor, signature, exceptions);
        if (methodVisitor == null || !methodName.equals(name)) {
            return methodVisitor;
        }

        return methodVisitorProvider.apply(methodVisitor, instructions);
    }

}
