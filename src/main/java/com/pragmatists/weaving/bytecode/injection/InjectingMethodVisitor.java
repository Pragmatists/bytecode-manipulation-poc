package com.pragmatists.weaving.bytecode.injection;

import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

public class InjectingMethodVisitor extends MethodVisitor {
    private final MethodVisitor originalMethodVisitor;
    private final List<Consumer<MethodVisitor>> operations;

    public InjectingMethodVisitor(MethodVisitor mv, List<Consumer<MethodVisitor>> operations) {
        super(ASM7, mv);
        this.originalMethodVisitor = mv;
        this.operations = operations;
    }

    @Override
    public void visitCode() {
        originalMethodVisitor.visitCode();
        operations.forEach(a -> a.accept(originalMethodVisitor));
    }
}
